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
| JDK | 25 |
| Spring Boot | 4.1.1 |
| Maven | 3.9.16 |
| PostgreSQL | Użytkownicy, produkty, alerty, historia cen i powiadomienia |
| Flyway | Migracje schematu bazy danych w środowisku produkcyjnym |
| Redis | Tokeny odświeżania z czasem wygaśnięcia |
| Apache Kafka | Kolejki scrapowania i wysyłki e-maili |
| Selenium + Jsoup | Odczyt danych z HTML |
| Spock + Groovy | Testy |
| Docker Compose | Lokalne uruchamianie PostgreSQL, Kafki i Redis |

Na komputerze uruchamiającym backend potrzebna jest przeglądarka Chrome i zgodny sterownik. Kod używa `ChromeDriver` w trybie headless. Selenium może pobierać sterownik automatycznie; pierwsze uruchomienie wymaga wtedy dostępu do sieci.

Sam plik Compose nie zawiera przeglądarki ani backendu.

## Szybki start

Wszystkie polecenia wykonuj w katalogu repozytorium. Przykłady używają Bash; w Windows odpowiednikiem `./mvnw` jest `mvnw.cmd`.

### 1. Ustaw zmienne środowiskowe

```bash
export PM_JWT_SECRET="$(openssl rand -base64 32)"
export PM_MAIL_USERNAME='twoj-adres@example.com'
export PM_MAIL_PASSWORD='haslo-do-serwera-smtp'
```

`PM_JWT_SECRET` musi być kluczem zakodowanym w Base64, zawierającym co najmniej 32 bajty po zdekodowaniu. Wygeneruj go raz i zachowaj w konfiguracji uruchomienia — zmiana klucza unieważnia podpisane nim JWT.

Sekretów nie zapisuj w repozytorium.

Domyślny SMTP to `smtp.gmail.com:587` z STARTTLS. Dane muszą pozwalać na uwierzytelnienie SMTP; dla konta Gmail może być potrzebne hasło aplikacji. Inny serwer można wskazać przez `PM_MAIL_HOST` i `PM_MAIL_PORT`, dostosowując również ustawienia TLS w konfiguracji.

W IDE ustaw:

- JDK 25,
- aktywny profil Spring `dev`,
- wymagane zmienne środowiskowe.

### 2. Uruchom usługi

```bash
docker compose up -d
```

| Usługa | Adres lokalny | Dane developerskie |
| --- | --- | --- |
| PostgreSQL | `localhost:5432` | Baza `pricemonitordb`, użytkownik `user`, hasło `password` |
| Kafka | `localhost:9092` | W sieci Compose: `kafka:29092` |
| Redis | `localhost:6379` | Bez hasła w konfiguracji lokalnej |

PostgreSQL i Redis korzystają z nazwanych wolumenów.

### 3. Uruchom backend

Jeżeli usługami z Compose zarządzasz samodzielnie:

```bash
SPRING_PROFILES_ACTIVE=dev \
PM_COMPOSE_ENABLED=false \
./mvnw spring-boot:run
```

Projekt ma również integrację Spring Boot Docker Compose. Alternatywnie można uruchomić:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

i pozostawić obsługę Compose aplikacji.

Backend domyślnie działa pod:

```text
http://localhost:8080
```

Następnie uruchom frontend pod:

```text
http://localhost:4200
```

Zarejestruj konto i otwórz link weryfikacyjny otrzymany w wiadomości e-mail.

---

## Konfiguracja

Podstawowe ustawienia znajdują się w:

```text
src/main/resources/application.yaml
```

Konfiguracja środowiska developerskiego:

```text
src/main/resources/application-dev.yaml
```

Konfiguracja produkcyjna:

```text
src/main/resources/application-prod.yaml
```

Profil Spring wybieraj przez zmienną:

```text
SPRING_PROFILES_ACTIVE
```

Lokalnie:

```bash
export SPRING_PROFILES_ACTIVE=dev
```

Na serwerze:

```bash
export SPRING_PROFILES_ACTIVE=prod
```

### Najważniejsze zmienne

| Ustawienie | Znaczenie / domyślna wartość |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Aktywny profil Spring: `dev` lub `prod` |
| `PM_JWT_SECRET` | Wymagany klucz do podpisywania JWT |
| `PM_MAIL_USERNAME`, `PM_MAIL_PASSWORD` | Dane logowania do SMTP |
| `PM_MAIL_HOST`, `PM_MAIL_PORT` | Serwer SMTP; domyślnie `smtp.gmail.com:587` |
| `app.client-url` / `PM_CLIENT_URL` | Adres frontendu: linki w e-mailach oraz dozwolone pochodzenie HTTP i WebSocket; w `dev`: `http://localhost:4200` |
| `spring.datasource.*` | Połączenie z PostgreSQL; można nadpisać przez `PM_DB_URL`, `PM_DB_USERNAME`, `PM_DB_PASSWORD` |
| `PM_DB_POOL_SIZE` | Maksymalna liczba połączeń do PostgreSQL; domyślnie 10 |
| `spring.kafka.bootstrap-servers` | Adres Kafki; zmienna `PM_KAFKA_BOOTSTRAP_SERVERS` |
| `spring.kafka.listener.concurrency` | Liczba równoległych konsumentów, domyślnie 4 |
| `spring.data.redis.host`, `spring.data.redis.port` | Adres Redis; zmienne `PM_REDIS_HOST` i `PM_REDIS_PORT` |
| `PM_REDIS_PASSWORD` | Hasło Redis w środowisku produkcyjnym |
| `PM_REDIS_SSL` | Włączenie TLS dla Redis; domyślnie `false` |
| `spring.jpa.hibernate.ddl-auto` | `dev`: `update`; `prod`: `validate` |
| `server.port` / `PM_SERVER_PORT` | Port HTTP, domyślnie 8080 |
| `PM_SERVER_ADDRESS` | Adres nasłuchiwania backendu; w produkcji domyślnie `127.0.0.1` |

Klucze `POSTGRES_*` używane przez obraz PostgreSQL nie są konfiguracją Spring Boot. Dane aplikacji przekazywane są przez zmienne `PM_DB_*`.

Zmiana hasła PostgreSQL w konfiguracji nie zmienia automatycznie hasła użytkownika w istniejącym wolumenie bazy.

Czasy ważności w `app.jwt` są podane w milisekundach:

- token weryfikacyjny: 3 godziny,
- token dostępu: 30 minut,
- token odświeżania: 30 dni.

Logowanie zapisuje ciasteczka `HttpOnly` z `SameSite=Strict`. Token odświeżania jest przechowywany w Redis.

---

## Profile środowiskowe

### Development

Profil:

```text
dev
```

używa:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update

  flyway:
    enabled: false
```

W środowisku developerskim Hibernate może więc automatycznie aktualizować strukturę lokalnej bazy danych podczas zmian encji.

Flyway jest wyłączony, dzięki czemu codzienna praca nad modelem nie wymaga tworzenia migracji dla każdej tymczasowej zmiany.

### Production

Profil:

```text
prod
```

używa:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
```

W produkcji Hibernate **nie tworzy ani nie modyfikuje schematu**.

Za strukturę PostgreSQL odpowiada Flyway.

Podczas uruchomienia:

1. aplikacja łączy się z PostgreSQL,
2. Flyway sprawdza historię migracji,
3. brakujące migracje są wykonywane w odpowiedniej kolejności,
4. Hibernate uruchamia walidację schematu,
5. backend startuje tylko wtedy, gdy schemat odpowiada aktualnym encjom.

Migracje powinny znajdować się w:

```text
src/main/resources/db/migration
```

Przykład:

```text
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_notifications.sql
└── V3__add_email_preferences.sql
```

Nie zmieniaj istniejącej migracji, która została już wykonana na środowisku produkcyjnym. Każda kolejna zmiana schematu powinna otrzymać nowy plik migracji.

---

## Jak działa monitorowanie

1. Podgląd URL trafia do scrapera przez Kafkę.
2. Produkt jest zapisywany przy tworzeniu alertu.
3. `ProductUpdateScheduler` uruchamia się o każdej pełnej godzinie (`0 0 * * * *`, zgodnie ze strefą czasową procesu JVM).
4. Scheduler kolejkuje produkty w paczkach po 100, według rosnącego ID.
5. Konsument pobiera stronę produktu i zwraca dane albo kod błędu.
6. Zmiana ceny zapisuje nowy wpis historii.
7. Aktywny alert z ceną docelową większą lub równą nowej cenie generuje powiadomienie i zostaje dezaktywowany.
8. E-mail jest wysyłany, jeśli użytkownik ma włączone alerty e-mail.
9. Preferencja użytkownika jest ponownie sprawdzana przed wysłaniem wiadomości.

Odpowiedź HTTP `404` lub `410` albo rozpoznany komunikat o nieistniejącej stronie powoduje usunięcie produktu, jego alertów, historii cen i wcześniejszych powiadomień powiązanych z produktem.

Użytkownicy mający alert otrzymują nowe powiadomienie z zapisaną nazwą produktu, bez powiązania z usuwanym rekordem.

Timeouty, błędy połączenia oraz odpowiedzi takie jak `403`, `429` czy `5xx` nie powodują automatycznego usunięcia produktu.

Oddzielna obsługa niedostępności towaru (`E016`) pozostawia produkt jako niedostępny, wysyła powiadomienia i usuwa jego alerty.

Powiadomienia o niedostępności oraz usunięciu trafiają do aplikacji, ale nie do kolejki alertów e-mail.

### Obsługiwane scrapery

Dedykowane scrapery obsługują między innymi:

- Amazon,
- Gunfire,
- Komputronik,
- Media Expert,
- Morele,
- RTV Euro AGD,
- Steam,
- Empik
- x-kom.

Dla pozostałych stron działa scraper ogólny oparty na metadanych.

Wynik zależy od struktury konkretnej strony, sposobu renderowania oraz zabezpieczeń sklepu. Obsługa domeny nie gwarantuje poprawnego odczytu każdego produktu.

---

## REST API i WebSocket

Swagger i OpenAPI są dostępne w profilu `dev`.

Profil `prod` wyłącza Swagger UI oraz endpointy OpenAPI.

### Development

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

OpenAPI YAML:

```text
http://localhost:8080/v3/api-docs.yaml
```

WebSocket/STOMP:

```text
ws://localhost:8080/ws
```

Subskrypcja użytkownika:

```text
/user/queue/notifications
```

### Główne grupy API

| Ścieżka | Zastosowanie |
| --- | --- |
| `/api/v1/auth` | Rejestracja, weryfikacja, logowanie, wylogowanie i odświeżanie sesji |
| `/api/v1/user` | Dane konta, ustawienia e-mail, zmiana i reset hasła |
| `/api/v1/product` | Lista produktów i `POST /info/preview` |
| `/api/v1/alert` | Lista, tworzenie, edycja i usuwanie alertów |
| `/api/v1/history` | Historia cen dla URL produktu |
| `/api/v1/notification` | Lista, licznik nieprzeczytanych, odczyt i usuwanie powiadomień |

Dostęp publiczny obejmuje między innymi:

- przeglądanie produktów,
- przeglądanie historii,
- podgląd URL,
- rejestrację,
- logowanie,
- weryfikację konta,
- odświeżanie sesji,
- odzyskiwanie hasła.

Konto użytkownika, alerty, powiadomienia oraz WebSocket wymagają poprawnego tokenu dostępu.

Edycja i usuwanie alertów są ograniczone do ich właściciela.

Pozostałe ścieżki są blokowane przez konfigurację Spring Security.

CORS i WebSocket dopuszczają dokładny adres zapisany w:

```text
PM_CLIENT_URL
```

Klient przeglądarkowy przesyła ciasteczka przez `withCredentials`.

Stronicowanie REST zaczyna się od:

```text
page=0
```

Sortowanie ma format:

```text
sort=name,asc
```

---

## Ochrona CSRF

Backend wymaga nagłówka:

```text
X-XSRF-TOKEN
```

zgodnego z ciasteczkiem:

```text
XSRF-TOKEN
```

dla żądań:

- `POST`,
- `PUT`,
- `PATCH`,
- `DELETE`.

Dotyczy to również części publicznych operacji wykonywanych przed zalogowaniem.

Publiczny endpoint:

```text
GET /api/v1/auth/csrf
```

inicjalizuje ciasteczko CSRF.

Odpowiedzi tego endpointu nie należy buforować.

Angular pobiera token automatycznie, jeśli go brakuje, i dodaje nagłówek CSRF do żądań API aplikacji.

Po:

- zalogowaniu,
- weryfikacji konta,
- wylogowaniu

backend wymienia token CSRF.

Ciasteczko CSRF jest celowo dostępne dla JavaScriptu.

Ciasteczka JWT pozostają `HttpOnly`.

W produkcji ciasteczka uwierzytelniające mają flagę:

```text
Secure
```

dlatego środowisko produkcyjne powinno działać przez HTTPS.

Frontend i backend najlepiej wystawiać pod jedną domeną przez reverse proxy.

Lokalnie używaj konsekwentnie:

```text
localhost
```

dla obu aplikacji.

Nie mieszaj:

```text
localhost
```

z:

```text
127.0.0.1
```

w adresach frontendu i backendu.

---

## Testy i budowanie

Uruchomienie testów:

```bash
./mvnw test
```

Pełna weryfikacja i build:

```bash
./mvnw clean verify
```

Testy znajdują się w:

```text
src/test/groovy
```

Używają mocków i testowej bazy H2. Nie wymagają działających sklepów, SMTP ani usług z Docker Compose.

Testy scrapowania nie zastępują sprawdzenia rzeczywistej strony sklepu.

Środowisko uruchomieniowe testów musi umożliwiać mechanizm podłączania agenta JVM używany przez Mockito.

### Plik wynikowy

Aktualna wersja projektu w `pom.xml`:

```xml
<version>1.0</version>
```

Po poprawnym buildzie aplikacja znajduje się w:

```text
target/price-monitor-1.0.jar
```

Przykładowe uruchomienie developerskie:

```bash
SPRING_PROFILES_ACTIVE=dev \
PM_COMPOSE_ENABLED=false \
java -jar target/price-monitor-1.0.jar
```

---

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

Migracje Flyway znajdują się w:

```text
src/main/resources/db/migration
```

---

# Konfiguracja produkcyjna

Docelowy wariant zakłada jedną domenę HTTPS oraz reverse proxy Nginx przed backendem.

Przykładowa architektura:

```text
Internet
   |
 HTTPS :443
   |
 Nginx
   |
   +------ / ---------------- frontend Angular
   |
   +------ /api/* ----------- Spring Boot :8080
   |
   +------ /ws ------------- Spring Boot :8080

Spring Boot
   |
   +------ PostgreSQL
   +------ Redis
   +------ Kafka
   +------ Chrome / Selenium
   +------ SMTP
```

Backend w produkcji powinien nasłuchiwać wyłącznie lokalnie, np.:

```text
127.0.0.1:8080
```

Nginx przekazuje do niego żądania `/api/` i `/ws`.

## Wymagania produkcyjne

Przed wdrożeniem zapewnij:

1. JDK 25,
2. Chrome lub Chromium oraz działający ChromeDriver,
3. PostgreSQL,
4. Redis,
5. Kafka,
6. reverse proxy Nginx,
7. domenę,
8. certyfikat HTTPS,
9. działające konto SMTP.

Schemat PostgreSQL jest zarządzany przez migracje Flyway wykonywane podczas startu backendu.

---

## Budowanie aplikacji produkcyjnej

```bash
./mvnw clean verify
```

Po buildzie:

```text
target/price-monitor-1.0.jar
```

---

## Zmienne produkcyjne

Przykładową konfigurację można przechowywać w:

```text
.env.prod
```

Na podstawie:

```text
.env.prod.example
```

Przykładowe wymagane wartości:

```dotenv
SPRING_PROFILES_ACTIVE=prod

PM_CLIENT_URL=https://monitor.example.com

PM_JWT_SECRET=CHANGE_ME

PM_DB_URL=jdbc:postgresql://127.0.0.1:5432/pricemonitordb
PM_DB_USERNAME=pricemonitor
PM_DB_PASSWORD=CHANGE_ME
PM_DB_POOL_SIZE=10

PM_KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092
PM_KAFKA_CONCURRENCY=4

PM_REDIS_HOST=127.0.0.1
PM_REDIS_PORT=6379
PM_REDIS_PASSWORD=CHANGE_ME
PM_REDIS_SSL=false

PM_MAIL_HOST=smtp.gmail.com
PM_MAIL_PORT=587
PM_MAIL_USERNAME=CHANGE_ME
PM_MAIL_PASSWORD=CHANGE_ME

PM_SERVER_ADDRESS=127.0.0.1
PM_SERVER_PORT=8080
```

`PM_CLIENT_URL` powinien zawierać publiczny adres HTTPS frontendu bez końcowego `/`.

Na przykład:

```text
https://monitor.example.com
```

### JWT

`PM_JWT_SECRET` powinien być trwałym kluczem Base64 zawierającym co najmniej 32 bajty po zdekodowaniu.

Można wygenerować go poleceniem:

```bash
openssl rand -base64 32
```

Nie generuj nowego sekretu przy każdym deploymencie. Zmiana klucza unieważni istniejące tokeny JWT.

---

## Uruchomienie produkcyjne

Jeżeli używasz pliku `.env.prod` w Bash:

```bash
set -a
source .env.prod
set +a

java -jar target/price-monitor-1.0.jar
```

Spring Boot nie wczytuje pliku `.env.prod` automatycznie.

Przy uruchamianiu aplikacji przez:

- `systemd`,
- Docker,
- Kubernetes,
- panel hostingowy

przekaż te same wartości przez mechanizm zmiennych środowiskowych danego środowiska.

---

## Kolejność uruchamiania produkcji

Przed uruchomieniem backendu powinny być dostępne:

1. PostgreSQL,
2. Redis,
3. Kafka.

Backend uruchamiaj z:

```text
SPRING_PROFILES_ACTIVE=prod
```

Podczas startu:

1. Flyway łączy się z PostgreSQL.
2. Sprawdza tabelę historii migracji.
3. Wykonuje wszystkie nowe migracje.
4. Hibernate uruchamia `ddl-auto: validate`.
5. Sprawdzana jest zgodność schematu z encjami.
6. Backend kończy start.

Jeżeli migracja jest niepoprawna albo struktura bazy nie odpowiada modelowi aplikacji, uruchomienie powinno zakończyć się błędem.

---

## Ustawienia profilu produkcyjnego

Profil `prod`:

- wyłącza integrację Spring Boot Docker Compose,
- wyłącza Swagger,
- wyłącza endpointy OpenAPI,
- wyłącza Open Session in View,
- ustawia ciasteczka jako `Secure`,
- włącza łagodne zamykanie serwera,
- używa `ddl-auto: validate`,
- korzysta z Flyway do zarządzania schematem,
- wymaga jawnej konfiguracji PostgreSQL,
- wymaga jawnej konfiguracji Kafka,
- wymaga konfiguracji Redis,
- domyślnie nasłuchuje na `127.0.0.1`.

Produkcja nie powinna korzystać z Hibernate `ddl-auto: update`.

---

## Flyway

Migracje zapisuj w:

```text
src/main/resources/db/migration
```

Nazwy plików powinny być zgodne z konwencją Flyway, np.:

```text
V1__initial_schema.sql
V2__add_user_email_preferences.sql
V3__make_notification_product_nullable.sql
```

### Zasady

Po wykonaniu migracji na współdzielonym lub produkcyjnym środowisku:

- nie zmieniaj jej zawartości,
- nie zmieniaj numeru,
- nie usuwaj jej.

Kolejne zmiany zapisuj jako nową migrację.

Przykład:

```text
V4__add_product_availability.sql
```

Przed wdrożeniem zmiany encji na produkcję przygotuj odpowiadającą jej migrację.

---

## Nginx i HTTPS

Frontend produkcyjny korzysta z bieżącej domeny, dlatego zalecane jest wystawienie Angulara oraz API pod jednym publicznym hostem.

Przykład:

```text
https://monitor.example.com
```

Nginx powinien:

- serwować statyczne pliki Angulara,
- proxy'ować `/api/` do Spring Boot,
- proxy'ować `/ws` jako WebSocket,
- przekierowywać HTTP na HTTPS,
- obsługiwać routing Angulara przez fallback do `index.html`.

Przykład dla Angulara:

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

Przykład API:

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;

    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Przykład WebSocket:

```nginx
location /ws {
    proxy_pass http://127.0.0.1:8080;

    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";

    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Profil produkcyjny ustawia ciasteczka `Secure`, dlatego korzystanie z HTTPS jest wymagane dla prawidłowego działania sesji użytkownika.

---

## Usługi infrastrukturalne

Developerski:

```text
compose.yaml
```

jest przeznaczony do lokalnego środowiska.

Nie należy bez zmian wystawiać go jako konfiguracji produkcyjnej.

W produkcji PostgreSQL, Redis i Kafka powinny być dostępne wyłącznie w prywatnej sieci lub na `localhost`.

Portów:

```text
5432
6379
9092
8080
```

nie należy wystawiać publicznie, jeżeli nie jest to świadomie wymagane przez architekturę.

Publicznie powinny być dostępne przede wszystkim:

```text
80
443
```

oraz port administracyjny SSH zgodnie z konfiguracją serwera.

---

## Jedna instancja backendu

Obecna wersja aplikacji zakłada pojedynczą instancję backendu.

`ProductUpdateScheduler` uruchamia się w każdej instancji aplikacji i nie posiada rozproszonej blokady.

Uruchomienie kilku instancji jednocześnie spowodowałoby wielokrotne wykonanie tego samego harmonogramu.

Przed skalowaniem backendu poziomo należy dodać mechanizm blokady rozproszonej lub wydzielić scheduler do osobnego procesu.

---

## Kafka

Aktualna konfiguracja klienta Kafki zakłada połączenie bez SASL/TLS w zaufanej sieci prywatnej.

Klaster wymagający:

- SASL,
- SSL/TLS,
- uwierzytelnienia

wymaga dodatkowej konfiguracji klienta Spring Kafka.

---

## Redis

Redis przechowuje tokeny odświeżania.

W środowisku produkcyjnym powinien być zabezpieczony hasłem.

Konfiguracja:

```text
PM_REDIS_PASSWORD
```

musi odpowiadać hasłu ustawionemu również po stronie serwera Redis.

Dla hostowanego Redis z TLS można ustawić:

```text
PM_REDIS_SSL=true
```

---

## PostgreSQL

Dane połączenia:

```text
PM_DB_URL
PM_DB_USERNAME
PM_DB_PASSWORD
```

Przykład:

```text
jdbc:postgresql://127.0.0.1:5432/pricemonitordb
```

Dla zewnętrznej bazy można dodać wymagane parametry TLS do JDBC URL zgodnie z wymaganiami dostawcy.

Maksymalny rozmiar puli Hikari:

```text
PM_DB_POOL_SIZE
```

domyślnie:

```text
10
```

---

## Typowe problemy

| Objaw | Co sprawdzić |
| --- | --- |
| Błąd kompilacji dotyczący Java 25 | `JAVA_HOME`, `java -version`, `javac -version` |
| Backend uruchamia się bez właściwej konfiguracji | Czy ustawiono `SPRING_PROFILES_ACTIVE=dev` lub `prod` |
| Błąd klucza JWT | Czy `PM_JWT_SECRET` jest ustawiony, poprawny Base64 i odpowiednio długi |
| Backend nie łączy się z PostgreSQL | `PM_DB_URL`, użytkownika, hasło, dostępność portu i logi PostgreSQL |
| Hibernate zgłasza błąd walidacji na prod | Czy wszystkie migracje Flyway zostały wykonane i czy schemat odpowiada encjom |
| Flyway zgłasza błąd checksum | Czy nie zmieniono wcześniej wykonanej migracji |
| Flyway nie widzi migracji | Czy pliki znajdują się w `src/main/resources/db/migration` i mają prawidłowe nazwy |
| Backend nie łączy się z Kafką | `PM_KAFKA_BOOTSTRAP_SERVERS`, działanie brokera i advertised listeners |
| Redis odrzuca połączenie | `PM_REDIS_HOST`, port, `PM_REDIS_PASSWORD`, ustawienia TLS |
| Brak wiadomości e-mail | Dane SMTP, logi backendu i Kafki, folder spam |
| Brak maili alertowych | Ustawienie alertów e-mail na koncie użytkownika |
| Scraping nie działa | Chrome/ChromeDriver, dostęp do sklepu, timeout, zmiana HTML albo blokada automatyzacji |
| Frontend nie utrzymuje sesji | HTTPS, `PM_CLIENT_URL`, cookies, CORS i reverse proxy |
| WebSocket nie działa | Proxy `/ws`, nagłówki `Upgrade` i `Connection`, sesję użytkownika |
| Bezpośrednie wejście na trasę Angulara daje 404 | Fallback Nginx do `index.html` |
| Produkcyjne cookies nie są wysyłane | Czy aplikacja działa przez HTTPS |
| Błąd po zmianie modelu danych | Czy razem ze zmianą encji została dodana nowa migracja Flyway |

