# Aplikacija FinanceBro

---
## Opis projekta
Cilj FinanceBro je ustvariti spletno aplikacijo za celovito upravljanje
osebnih financ, ki bo uporabnikom omogočala  spremljanje prihodkov, odhodkov,
varčevanj in investicij ter varčevalnih ciljev in proračuna.

Aplikacija bo nudila funkcionalnosti za vnos transakcij, spremljanje vrednosti investicij v
kriptovalute in delnice ter vnos proračunov in dolgov. Aplikacija rešuje problem nepreglednega in neučinkovitega upravljanja osebnih financ, saj bo aplikacija vse naštete
funkcionalnosti združila na enem mestu.

---
## Tipi uporabnikov
### 1. Gost
Gostujoči uporabniki so obiskovalci, ki se niso registrirali ali prijavili v aplikacijo.

Njihov dostop je omejen na uvodno stran aplikacije - ta ponuja osnovne informacije o aplikaciji in njenih funkcijah.

Omejen dostop: Gostje nimajo dostopa do glavnih funkcionalnosti aplikacije, kot so dodajanje stroškov, sledenje naložbam ali ogled poročil.

### 2. Običajen uporabnik

Običajni uporabniki so osebe, stare 18 let ali več in so registrirane in prijavljene v aplikacijo. Imajo dostop do vseh glavnih funkcionalnosti aplikacije:

**Glavne funkcionalnosti:**
- Dodajanje dnevnih prihodkov in odhodkov.
- Upravljanje in pregled naložb.
- Nastavitev varčevalnih ciljev in sledenje napredku.
- Nastavitev mesečnega proračuna in sledenje napredku.
- Beleženje svojih dolgov in odplačevanje le-teh. 

**Ciljna skupina:** 
- Ta tip uporabnika je idealen za posameznike, ki iščejo preprost in učinkovit način za upravljanje osebnih financ.

---
### Gostovanje aplikacije

Aplikacija je naložena na kubernetes gručo in gostuje na naslovu: https://financebro.app

---
### Lokalni zagon aplikacije - navodila za ostale razvijalce

Aplikacijo je mogoče izvajati lokalno za namene razvoja ali testiranja. Spodaj so podrobnosti za namestitev in nastavitev.

**Predpogoji:**
-  **Java**: Java 17, Java Development Kit (JDK) 17.
- **Apache Maven**: Maven 3.9.9 ali novejša, pot do nameščenega Apache Maven mora biti dodana v sistemsko spremenljivko PATH
- **Docker**: Docker 24.0.6
- **Angular CLI**: Angular CLI 19.0.6.

**Namestitev:**
1. Klonirajte oddaljeni repozitorij v svoj lokalni:
    ```bash
   git clone https://github.com/PRPO7-SS/Project-07.git
   cd Project-07

2. Naložite vse odvisnosti za zagon uporabniškega vmesnika
    ```bash
    cd frontend
    npm install

**Zagon aplikacije**
1. Zgradite projekt
    ```bash
   cd backend
   mvn clean package
   
2. Zagon backenda z Dockerjem
    Prepričajte se, da je Docker nameščen in deluje v vašem sistemu. 
    Zaženite vsebnik za podatkovno bazo mongo in ostale mikrostoritve z ukazom
    ```bash
   cd backend
   docker-compose up -d --build
   
To bo zagnalo primerek MongoDB na vratih 27017 in vse mikrostoritve: 
user-service na vratih 8080, investment-service na vratih 8085, transaction-service na vratih 8081,
savings-goal na vratih 8084, budget-service na vratih 8083 in debt-tracking-service na vratih 8086.

3. Zagon uporabniškega vmesnika
    Prestavite se v frontend direktorij in zaženite osprednji del aplikacije:
    ```bash
   cd frontend
   ng serve

4. Lokalni dostop do MongoDB baze
    Za lokalni dostop do MongoDB baze, uporabo baze financeApp in dostop do zbirk uporabimo spodnji ukaz.
    ```bash
    docker exec -it mongodb_new mongosh
    use financeApp
    show collections

5. Dostop do RabbitMQ UI
    Za dostop do RabbiMQ umesnika, v brskalniku vpišemo spodnji ukaz in se prijavimo z uporabniškim imenom in geslom.
    ```bash
    http://localhost:15672
   
Frontend bo privzeto deloval na http://localhost:4200

---

### Konfiguracija
Za lokalno delovanje aplikacije je potrebno nastaviti okoljske spremenljivke.
V direktoriju backend ustvarite datoteko .env, innastavite naslednje okoljske spremenljivke:

**MONGO_URI**=mongodb://mongodb:27017

**JWT_SECRET**=aU0Gx6LJbYukM9DJmcFZjx0q3wx+TnpRkmRu2F7rx3Y=

**REFRESH_TOKEN_SECRET**=g5gXa5lqbEVX2DVOGPJcOSbeBH9QD51istNulnB1tJZdIlP6ZWVS5YuJcua2Hg9dd7Dw35n7AV9dWOTXPGIb2g

**API_KEY**=fa70322f056548238c1dabc6a8eba98a

**API_URL**=https://api.twelvedata.com

**RABBITMQ_PORT**=5672

**RABBITMQ_USER**=guest

**RABBITMQ_PASS**=guest
    
V datoteki frontend/src/environments/environments.ts nastavite spremeljivko
twelveDataApiKey na vrednost fa70322f056548238c1dabc6a8eba98a.

---

### Arhitektura

Arhitektura temelji na mikrostoritevni zasnovi. 
Vsaka mikrostoritev je razvijana ločeno in bo odgovorna za svojo domeno:
- Uporabniška mikrostoritev: Prijava, registracija, profil.
- Transakcijska mikrostoritev: Dnevni vnosi prihodkov in odhodkov,
  kategorizacija.
- Investicijska mikrostoritev: Vnos, pregled in spremljanje vrednosti investicij v delnice in kriptovalute. \
Vsebuje integracijo zunanjega API-ja https://api.twelvedata.com za izračun trenutne vrednosti investicije.
- Varčevalna mikrostoritev: dodajanje, spremljanje, posodobitev varčevalnih ciljev uporabnika
- Mikrostoritev za proračune: upravljanje proračunov uporabnika
- Mikrostoritev za dolgove: upravljanje in vpogled v dolgove uporabnika.

**Frontend:** Angular (za razvoj uporabniškega vmesnika) \
**Backend:** Java (za razvoj RESTful API-jev) \
**Baza podatkov:** MongoDB (za shranjevanje podatkov) \
**Kontejnerizacija in orkestracija:** Docker in Kubernetes (za mikrostoritevno
  arhitekturo) \
**API integracije:** API-ji za kriptovalute (za sledenje vrednosti investicij) \
**Razvojno okolje:** GitHub organizacija za verzioniranje, CI/CD orodja

---

### Dokumentacija API
Dokumentacija API za končne točke posamezne mikrostoritve je ustvarjena s pomočjo odvisnosti kumuluzee-openapi-mp, microprofile-config-api in kumuluzee-config-mp. \
Dostopate lahko na naslednji poti, ko aplikacija deluje:
- **Uporabniška mikrostoritev**: [http://localhost:8080/api-docs/ui](http://localhost:8080/api-docs/ui)
- **Transakcijska mikrostoritev**: [http://localhost:8081/api-docs/ui](http://localhost:8081/api-docs/ui)
- **Investicijska mikrostoritev**: [http://localhost:8085/api-docs/ui](http://localhost:8085/api-docs/ui)
- **Varčevalna mikrostoritev**: [http://localhost:8084/api-docs/ui](http://localhost:8084/api-docs/ui)
- **Mikrostoritev za proračune**: [http://localhost:8083/api-docs/ui](http://localhost:8083/api-docs/ui)
- **Mikrostoritev za dolgove**: [http://localhost:8086/api-docs/ui](http://localhost:8086/api-docs/ui)
---

### Seznam uporabljenih knjižnic

#### 1. Uporabljene knjižnice v zaledju aplikacije
   1. **Java EE**:\
    Knjižnice: 
    - javax.ws.rs.*  – za implementacijo REST API-jev.
    - javax.ws.rs.core.* – za delo z HTTP zahtevami in odgovori, kot so piškotki in vrste medijev.\
    **Uporaba:** Omogoča gradnjo REST API-jev.
   
   2. **JSON Web Tokens (JWT)**: \
   Knjižnice: 
      - io.jsonwebtoken.Claims
      - io.jsonwebtoken.ExpiredJwtException
      - io.jsonwebtoken.Jwts
      - io.jsonwebtoken.SignatureAlgorithm \
      **Uporaba:** Ustvarjanje, preverjanje in analiza JWT žetonov za avtentikacijo.

   3. **Bcrypt** \
   Knjižnice:
   - org.mindrot.jbcrypt.BCrypt \
   **Uporaba:** Hashiranje gesel za varno shranjevanje in preverjanje gesel uporabnikov.
   
   4. **MongoDB Client Library:** \
   Knjižnice: 
   - org.bson.types.ObjectId 
   - org.bson.Document
   - com.mongodb.client.MongoClient 
   - com.mongodb.client.MongoCollection
   - com.mongodb.client.MongoDatabase \
   **Uporaba:** Povezava z MongoDB bazo podatkov, delo z zbirkami in dokumenti, identifikacija dokumentov v bazi MongoDB.

   5. **MicroProfile OpenAPI:** \
   Knjižnice:
   - org.eclipse.microprofile.openapi.annotations.* \
   **Uporaba:** Avtomatizirano dokumentiranje REST API-jev z uporabo OpenAPI specifikacije.
   
   6. **JSON Parsing**
   Knjižnice:
   - org.json.JSONObject \
   **Uporaba:** Delo z JSON formatom podatkov pri obdelavi HTTP zahtev in odgovorov.

   7. **Java Logging:** \
    Knjižnice:
   - java.util.logging.Logger \
   **Uporaba:** Beleženje dogodkov in napak v aplikaciji.
   
   8. **HTTP Komunikacija:** \
   Knjižnice:
   - java.net.HttpURLConnection
   - java.net.URL
   - java.util.Scanner \
   **Uporaba:** Izvajanje HTTP zahtev na zunanje storitve.

   9. **CDI (Context and Dependency Injection):** \
   Knjižnice:
   - javax.enterprise.context.ApplicationScoped
   - javax.inject.Inject
   - javax.annotation.PostConstruct \
   **Uporaba:** Upravljanje življenjskega cikla in odvisnosti komponent.

   10. **JAX-RS za HTTP filtre:**  \
   Knjižnice:
   -  javax.ws.rs.container.ContainerRequestContext
   - javax.ws.rs.container.ContainerResponseContext
   - javax.ws.rs.container.ContainerResponseFilter
   - javax.ws.rs.ext.Provider  \
    **Uporaba:** Uporaba filtrov za pred- in post-obdelavo HTTP zahtev in odgovorov.

   11. **JAX-RS prioritete:**  
       Knjižnice:
       - javax.annotation.Priority
       - javax.ws.rs.Priorities  
         **Uporaba:** Določanje vrstnega reda izvajanja filtrov.

   12. **JAX-RS večvrednostni slovar:**  
       Knjižnice:
       - javax.ws.rs.core.MultivaluedMap  
         **Uporaba:** Upravljanje večvrednostnih HTTP glav in parametrov.

   13. **Java IO:**  
       Knjižnice:
       - java.io.IOException  
         **Uporaba:** Upravljanje vhodno-izhodnih napak pri obdelavi zahtev in odgovorov.


#### 2. Uporabljene knjižnice v osprednem delu aplikacije

Knjižnica: \
    - Chart.js \
    **Uporaba:** Ustvarjanje interaktivnih in odzivnih grafov za vizualizacijo podatkov.