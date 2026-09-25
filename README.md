# Price Monitor — backend

Backend aplikacji do monitorowania cen produktów ze sklepów internetowych. Pobiera dane ze stron produktów, przechowuje historię cen i powiadamia użytkownika po osiągnięciu ustawionej ceny. Interfejs użytkownika znajduje się w osobnym repozytorium [**Price Monitor Web**](https://github.com/jakubstraszak99-sudo/Price-Monitor-Web).

## Funkcjonalności

- Rejestracja, weryfikacja adresu e-mail, logowanie i wylogowanie.
- Zmiana hasła oraz odzyskiwanie dostępu przez link przesłany e-mailem.
- Podgląd produktu na podstawie URL, wyszukiwanie, sortowanie i stronicowanie listy produktów.
- Tworzenie alertów cenowych, zmiana ceny docelowej, włączanie, wyłączanie i usuwanie alertów.
- Historia cen i cykliczne sprawdzanie produktów.
- Powiadomienia w aplikacji, dostarczane także na żywo przez WebSocket.
- Alerty e-mail z możliwością wyłączenia ich w ustawieniach konta. Preferencja nie wyłącza wiadomości weryfikacyjnych i odzyskiwania hasła.
- Usuwanie produktów z nieistniejących stron wraz z powiadomieniem obserwujących.

Logowanie przez Google nie jest częścią obecnej wersji.

## Technologie i wymagania

| Element | Wersja / zastosowanie |
| --- | --- |
| JDK | 25; wymagany pełny JDK, nie samo JRE |
| Spring Boot | 4.1.1 |
| Maven | 3.9.16 przez dołączony Maven Wrapper |
| PostgreSQL | Użytkownicy, produkty, alerty, historia cen i powiadomienia |
| Redis | Tokeny odświeżania z czasem wygaśnięcia |
| Apache Kafka | Kolejki scrapowania i wysyłki e-maili |
| Selenium + Jsoup | Renderowanie stron w Chrome i odczyt danych z HTML |
| Spock + Groovy | Testy; H2 do testowania operacji na bazie |
| Docker Compose | Lokalne uruchamianie PostgreSQL, Kafki i Redis |

Na komputerze uruchamiającym backend potrzebna jest przeglądarka Chrome i zgodny sterownik. Kod używa `ChromeDriver` w trybie headless. Selenium może pobierać sterownik automatycznie; pierwsze uruchomienie wymaga wtedy dostępu do sieci. Sam plik Compose nie zawiera przeglądarki ani backendu.

## Szybki start

Wszystkie polecenia wykonuj w katalogu repozytorium. Przykłady używają Bash; w Windows odpowiednikiem `./mvnw` jest `mvnw.cmd`.

### 1. Ustaw zmienne środowiskowe

```bash
export PM_JWT_SECRET="$(openssl rand -base64 32)"
export PM_MAIL_USERNAME='twoj-adres@example.com'
export PM_MAIL_PASSWORD='haslo-do-serwera-smtp'
```

`PM_JWT_SECRET` musi być kluczem zakodowanym w Base64, zawierającym co najmniej 32 bajty po zdekodowaniu. Wygeneruj go raz i zachowaj w konfiguracji uruchomienia — zmiana klucza unieważnia podpisane nim JWT. Sekretów nie zapisuj w repozytorium.

Domyślny SMTP to `smtp.gmail.com:587` z STARTTLS. Dane muszą pozwalać na uwierzytelnienie SMTP; dla konta Gmail może być potrzebne hasło aplikacji. Inny serwer można wskazać przez `PM_MAIL_HOST` i `PM_MAIL_PORT`, dostosowując również ustawienia TLS w konfiguracji.

W IDE ustaw JDK 25, profil `dev` i te same zmienne w konfiguracji uruchomienia. Samo utworzenie pliku `.env` nie wczytuje jego zawartości do procesu Java.

### 2. Uruchom usługi

```bash
docker compose up -d
```

| Usługa | Adres lokalny | Dane developerskie |
| --- | --- | --- |
| PostgreSQL | `localhost:5432` | Baza `pricemonitordb`, użytkownik `user`, hasło `password` |
| Kafka | `localhost:9092` | W sieci Compose: `kafka:29092` |
| Redis | `localhost:6379` | Bez hasła w konfiguracji lokalnej |

PostgreSQL i Redis korzystają z nazwanych wolumenów. `docker compose down` zatrzymuje i usuwa kontenery, zachowując te wolumeny. Obecny Compose nie definiuje trwałego wolumenu Kafki.

### 3. Uruchom backend

```bash
PM_COMPOSE_ENABLED=false ./mvnw spring-boot:run
```

Wyłączenie integracji Compose w tym poleceniu oznacza, że usługami zarządzasz samodzielnie w kroku 2. Projekt ma również integrację Spring Boot Docker Compose: alternatywnie można uruchomić `./mvnw spring-boot:run` i pozostawić obsługę Compose aplikacji.

Backend domyślnie działa pod `http://localhost:8080`. Następnie uruchom frontend na `http://localhost:4200`, zarejestruj konto i otwórz link weryfikacyjny z wiadomości e-mail.

## Konfiguracja

Podstawowe ustawienia znajdują się w [application.yaml](src/main/resources/application.yaml), a połączenia lokalne i adres frontendu w [application-dev.yaml](src/main/resources/application-dev.yaml). Profil wybiera `PM_PROFILE`: domyślnie `dev`, na serwerze ustaw `prod`. Konfiguracja produkcyjna znajduje się w [application-prod.yaml](src/main/resources/application-prod.yaml).

| Ustawienie | Znaczenie / domyślna wartość |
| --- | --- |
| `PM_JWT_SECRET` | Wymagany klucz do podpisywania JWT |
| `PM_MAIL_USERNAME`, `PM_MAIL_PASSWORD` | Dane logowania do SMTP |
| `app.client-url` / `PM_CLIENT_URL` | Adres frontendu: linki w e-mailach oraz dozwolone pochodzenie HTTP i WebSocket; w `dev`: `http://localhost:4200` |
| `spring.datasource.*` | Połączenie z PostgreSQL; można nadpisać przez `PM_DB_URL`, `PM_DB_USERNAME`, `PM_DB_PASSWORD` |
| `spring.kafka.bootstrap-servers` | Adres Kafki; zmienna `PM_KAFKA_BOOTSTRAP_SERVERS` |
| `spring.kafka.listener.concurrency` | Liczba równoległych konsumentów, domyślnie 4 |
| `spring.data.redis.host`, `spring.data.redis.port` | Dla innego adresu Redis ustaw `PM_REDIS_HOST` i `PM_REDIS_PORT`; lokalnie używane są `localhost:6379` |
| `spring.jpa.hibernate.ddl-auto` | `dev`: `update`; `prod`: `validate` |
| `server.port` / `PM_SERVER_PORT` | Port HTTP, domyślnie 8080 |

Zmienne aplikacji mają przedrostek `PM_`; stare nazwy `JWT_SECRET`, `MAIL_USERNAME` i `MAIL_PASSWORD` trzeba przemianować. Klucze `POSTGRES_*` w Compose są wymagane przez obraz PostgreSQL, ale wartości pobierają ze zmiennych `PM_DB_*`. Zmiana tych wartości nie zmienia hasła istniejącej bazy w wolumenie.

Czasy ważności w `app.jwt` są podane w milisekundach: token weryfikacyjny 3 godziny, token dostępu 30 minut, token odświeżania 30 dni. Logowanie zapisuje ciasteczka `HttpOnly` i `SameSite=Strict`; token odświeżania jest przechowywany w Redis.

## Jak działa monitorowanie

1. Podgląd URL trafia do scrapera przez Kafkę. Produkt jest zapisywany przy tworzeniu alertu.
2. `ProductUpdateScheduler` uruchamia się o każdej pełnej godzinie (`0 0 * * * *`, strefa czasowa procesu JVM). Kolejkuje produkty w paczkach po 100, według rosnącego ID.
3. Konsument pobiera stronę i zwraca dane produktu albo kod błędu.
4. Zmiana ceny zapisuje historię. Aktywny alert z ceną docelową większą lub równą nowej cenie generuje powiadomienie i zostaje dezaktywowany. E-mail jest wysyłany, jeśli użytkownik go włączył; preferencja jest ponownie sprawdzana przed wysyłką.
5. Odpowiedź HTTP 404/410 albo rozpoznany komunikat o nieistniejącej stronie powoduje usunięcie produktu, alertów, historii cen i wcześniejszych powiadomień powiązanych z produktem. Użytkownicy mający alert otrzymują nowe powiadomienie z zapisaną nazwą, bez powiązania z usuwanym rekordem.

Timeouty, błędy połączenia i odpowiedzi takie jak 403, 429 czy 5xx nie usuwają produktu. Oddzielna obsługa niedostępności towaru (`E016`) pozostawia produkt jako niedostępny, wysyła powiadomienia i usuwa jego alerty. Powiadomienia o niedostępności i usunięciu trafiają do aplikacji, nie do kolejki alertów e-mail.

Dedykowane scrapery obsługują Amazon, Gunfire, Komputronik, Media Expert, Morele, RTV Euro AGD, Steam i x-kom. Dla pozostałych stron działa scraper ogólny oparty na metadanych. Wynik zależy od struktury strony i ograniczeń sklepu; obsługa domeny nie gwarantuje odczytu każdego produktu.

## REST API i WebSocket

Swagger i OpenAPI są dostępne w `dev`; profil `prod` je wyłącza.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`.
- WebSocket/STOMP: `ws://localhost:8080/ws`, subskrypcja użytkownika: `/user/queue/notifications`.

Główne grupy API:

| Ścieżka | Zastosowanie |
| --- | --- |
| `/api/v1/auth` | Rejestracja, weryfikacja, logowanie, wylogowanie, odświeżanie sesji |
| `/api/v1/user` | Dane konta, ustawienia e-mail, zmiana i reset hasła |
| `/api/v1/product` | Lista produktów i `POST /info/preview` |
| `/api/v1/alert` | Lista, tworzenie, edycja i usuwanie alertów |
| `/api/v1/history` | Historia cen dla URL produktu |
| `/api/v1/notification` | Lista, licznik nieprzeczytanych, odczyt i usuwanie powiadomień |

Dostęp publiczny obejmuje przeglądanie produktów i historii, podgląd URL oraz operacje logowania, rejestracji, weryfikacji i odzyskiwania sesji lub hasła. Konto, alerty, powiadomienia i WebSocket wymagają poprawnego tokenu dostępu. Edycja i usuwanie alertów są ograniczone do właściciela. Pozostałe ścieżki są blokowane. CORS i WebSocket dopuszczają dokładny adres z `PM_CLIENT_URL`, bez wzorców domen.

Szczegółowe parametry i modele opisuje Swagger. Stronicowanie REST zaczyna się od `page=0`; sortowanie ma postać `sort=name,asc`. Klient przeglądarkowy przesyła ciasteczka przez `withCredentials`.

## Testy i budowanie

```bash
./mvnw test
./mvnw clean verify
```

Testy znajdują się w `src/test/groovy`. Używają mocków i testowej bazy H2; nie wymagają działających sklepów, SMTP ani usług z Compose. Testy scrapowania nie zastępują sprawdzenia rzeczywistej strony sklepu. Środowisko uruchomieniowe testów musi umożliwiać mechanizm podłączania agenta JVM używany przez Mockito.

Plik aplikacji po budowaniu: `target/price-monitor-0.0.1-SNAPSHOT.jar`. Przy ustawionych zmiennych środowiskowych i uruchomionych usługach:

```bash
java -jar target/price-monitor-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --spring.docker.compose.enabled=false
```

## Struktura kodu

| Katalog w `src/main/java/com/github/pricemonitor` | Zawartość |
| --- | --- |
| `api`, `api/resource` | Kontrakty REST i kontrolery |
| `service`, `service/impl` | Logika aplikacji |
| `model`, `repository` | Encje, DTO, mapowanie MapStruct i dostęp do bazy |
| `scraper` | Wspólna obsługa przeglądarki i scrapery sklepów |
| `kafka`, `scheduler` | Komunikaty, konsumenci i harmonogram |
| `security`, `redis`, `websocket` | JWT, tokeny odświeżania i powiadomienia na żywo |
| `config`, `properties` | Konfiguracja Spring i ustawienia aplikacji |

## Konfiguracja produkcyjna

Przygotowany wariant zakłada jedną domenę HTTPS oraz Nginx na tym samym serwerze co backend. Backend domyślnie słucha na `127.0.0.1:8080`. Konfiguracja Nginx jest w repozytorium frontendu: `deploy/nginx.conf.example`; przekazuje `/api/` i `/ws` do backendu.

1. Zapewnij JDK 25, Chrome/ChromeDriver, PostgreSQL z gotowym schematem, Redis i Kafkę.
2. Zbuduj aplikację: `./mvnw clean verify`.
3. Skopiuj [.env.prod.example](.env.prod.example) do `.env.prod`. Uzupełnij adresy i wszystkie wartości `CHANGE_ME`, a `PM_CLIENT_URL` ustaw na publiczny adres HTTPS, bez końcowego ukośnika.
4. Wczytaj zmienne do procesu i uruchom JAR:

```bash
set -a
source .env.prod
set +a
java -jar target/price-monitor-0.0.1-SNAPSHOT.jar
```

Powyższe polecenia są dla Bash. Sam Spring nie wczytuje `.env.prod`, a plik jest ignorowany przez Git. Przy uruchamianiu przez usługę systemową lub panel hostingu przekaż te same zmienne przez mechanizm środowiska procesu.

| Zmienna | Znaczenie w profilu produkcyjnym |
| --- | --- |
| `PM_PROFILE=prod` | Włącza profil produkcyjny |
| `PM_CLIENT_URL` | Wymagany publiczny adres HTTPS frontendu |
| `PM_JWT_SECRET` | Wymagany trwały klucz Base64, minimum 32 bajty po zdekodowaniu |
| `PM_DB_URL`, `PM_DB_USERNAME`, `PM_DB_PASSWORD` | Wymagane dane PostgreSQL; parametry TLS można dodać do URL JDBC |
| `PM_DB_POOL_SIZE` | Maksymalna liczba połączeń, domyślnie 10 |
| `PM_KAFKA_BOOTSTRAP_SERVERS` | Wymagany adres Kafki w sieci prywatnej |
| `PM_KAFKA_CONCURRENCY` | Liczba równoległych konsumentów, domyślnie 4 |
| `PM_REDIS_HOST`, `PM_REDIS_PASSWORD` | Wymagany host i hasło; hasło musi być również ustawione na serwerze Redis |
| `PM_REDIS_PORT`, `PM_REDIS_SSL` | Domyślnie 6379 i `false`; dla Redis z TLS ustaw `true` |
| `PM_MAIL_USERNAME`, `PM_MAIL_PASSWORD` | Wymagane dane SMTP |
| `PM_MAIL_HOST`, `PM_MAIL_PORT` | Domyślnie Gmail i 587; wymagane STARTTLS |
| `PM_MAIL_FROM` | Opcjonalny nadawca, domyślnie nazwa Price Monitor i adres `PM_MAIL_USERNAME` |
| `PM_SERVER_ADDRESS`, `PM_SERVER_PORT` | Domyślnie `127.0.0.1:8080`; w kontenerze ustaw `0.0.0.0` i ogranicz dostęp do proxy |

Profil `prod` wyłącza Docker Compose, Swagger, logowanie SQL i Open Session in View. Włącza ciasteczka `Secure`, łagodne zamykanie oraz `ddl-auto: validate`. Nie tworzy ani nie aktualizuje schematu: przed pierwszym startem przygotuj bazę na podstawie aktualnych encji i zastosuj migracje, w tym zmianę powiadomień opisaną wyżej. Pusta baza spowoduje błąd walidacji. Projekt nie zawiera automatycznego narzędzia migracji.

`compose.yaml` pozostaje konfiguracją developerską, nie produkcyjną. Klient Kafki korzysta z PLAINTEXT w sieci prywatnej; klaster wymagający SASL/TLS wymaga dodatkowej konfiguracji klienta. Ten wariant zakłada jedną instancję backendu, ponieważ scheduler nie ma blokady między instancjami. Domena, certyfikat HTTPS, usługi bazodanowe i uruchomienie procesu wymagają przygotowania na serwerze docelowym.

## Typowe problemy

| Objaw | Co sprawdzić |
| --- | --- |
| Błąd kompilacji dotyczący Java 25 | `JAVA_HOME`, `java -version` oraz `javac -version` |
| Błąd klucza JWT lub brak placeholdera | Czy `PM_JWT_SECRET` jest ustawiony, poprawny Base64 i ma odpowiednią długość |
| Backend nie łączy się z usługami | Profil `dev`, `docker compose ps`, zajęte porty i adresy połączeń |
| Brak wiadomości e-mail | Dane SMTP, logi backendu i Kafki, folder spam; dla alertów także ustawienia konta |
| Scraping nie działa | Chrome/sterownik, dostęp do sklepu, timeout, zmiana HTML lub blokada automatyzacji |
| Błąd `notifications.product_id` przy usuwaniu produktu | Migracja opisana w sekcji bazy danych |
| Frontend nie utrzymuje sesji | Zgodność `PM_CLIENT_URL`, protokołu i hosta; nie mieszaj `localhost` z `127.0.0.1` |

### Ochrona CSRF

Backend wymaga nagłówka `X-XSRF-TOKEN` zgodnego z ciasteczkiem `XSRF-TOKEN` dla żądań POST, PUT, PATCH i DELETE, także przed zalogowaniem. Publiczny `GET /api/v1/auth/csrf` inicjalizuje ciasteczko; odpowiedzi nie należy buforować. Angular pobiera je automatycznie, jeśli go brakuje, i dodaje nagłówek tylko do API aplikacji. Po zalogowaniu, weryfikacji konta i wylogowaniu backend wymienia token.

Ciasteczko CSRF jest celowo dostępne dla JavaScript; ciasteczka JWT pozostają HttpOnly. W produkcji mają flagę Secure. Frontend i API powinny działać pod wspólną domeną przez reverse proxy; lokalnie używaj `localhost` dla obu aplikacji (nie mieszaj z `127.0.0.1`). Klienci inni niż przeglądarka również muszą zachowywać ciasteczka i przesyłać nagłówek CSRF.
