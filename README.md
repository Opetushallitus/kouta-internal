# kouta-internal

## 1. Palvelun tehtävä

Tarjoaa rajapinnan OPH:n sisäisille palveluille uuden koulutustarjonnan indeksoituun dataan.

## 2. Arkkitehtuuri

Kouta-internal on Scalatralla toteutettu HTTP API, joka tarjoilee kouta-indeksoijan Elasticsearchiin indeksoimaa 
kouta-backendin dataa. Kouta-internal tallentaa ainoastaan sisäisen session omaan postgresql-kantaan.

Kirjoitushetkellä tiedossa olevia kouta-internaliin integroitujia on ainoastaan ataru.

## 3. Kehitysympäristö

### 3.1. Esivaatimukset

Asenna haluamallasi tavalla koneellesi
1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) + [scala plugin](https://plugins.jetbrains.com/plugin/1347-scala)
2. [Docker](https://www.docker.com/get-started) (postgresia ja elasticsearchia varten)
3. [Maven](https://maven.apache.org/) Jos haluat ajaa komentoriviltä Mavenia,
   mutta idean Mavenilla pärjää kyllä hyvin, joten tämä ei ole pakollinen

Lisäksi tarvitset Java SDK:n ja Scala SDK:n (Unix pohjaisissa käyttöjärjestelmissä auttaa esim. [SDKMAN!](https://sdkman.io/)). Katso [.travis.yml](.travis.yml) mitä versioita sovellus käyttää.
Kirjoitushetkellä käytössä openJDK11 ja scala 2.12.10.   
(TODO huom. travis ymlissä 2.12.2 mutta pom.xml:ssä 2.12.10, pitäisi nostaa traviksen versiota!).

PostgreSQL kontti-image buildataan (täytyy tehdä vain kerran) komennnolla: 
``` shell
# projektin juuressa
cd postgresql/docker
docker build --tag koutainternal-postgres .
```

Kopioi testikonfiguraatio lokaalia kehitystä varten '/src/test/resources/test-vars.yml' -> '/src/test/resources/dev-vars.yml'. 
Dev-vars.yml on ignoroitu Gitissä ettei salasanat valu repoon.

Asetuksia voi muuttaa muokkaamalla '/src/test/resources/dev-vars.yml'-tiedostoa, tai
ainakin luulen näin, koska kouta-backendissa on vastaava rakenne. Kunhan joku selvittää 
konfig-tiedoston toiminnan, toivottavasti päivittää myös tämän osion. 

### 3.2. Testien ajaminen

Testejä varten täytyy Docker daemon olla käynnissä.

Testit voi ajaa ideassa Maven ikkunasta valitsemalla test lifecycle phasen kouta-internalin kohdalta
tai avaamalla Edit Configurations valikon ja luomalla uuden Maven run configurationin jolle laitetaan 
working directoryksi projektin juurikansio ja Command line komennoksi test. Tämän jälkeen konfiguraatio ajoon.

Yksittäisen testisuiten tai testin voi ajaa ottamalla right-click halutun testiclassin tai funktion päältä, run -> scalaTest.

Jos Maven on asennettuna, voi testit ajaa myös komentoriviltä `mvn test` komennolla tai rajaamalla 
ajettavien testejä `mvn test -Dsuites="<testiluokan nimet pilkulla erotettuna>"`. 
Esimerkiksi `mvn test -Dsuites="fi.oph.kouta.internal.integration.HakukohdeSpec"` 

Testit käynnistävät Elasticsearchin ja postgresql:n docker-konteissa satunnaisiin vapaisiin portteihin.

### 3.3. Migraatiot

Migraatiot ajetaan automaattisesti testien alussa tai kun kouta-internal käynnistetään.
Kirjoitushetkellä projektissa on ainoastaan yksi migraatio, jossa luodaan sessio-taulu kantaan.

### 3.4. Ajaminen lokaalisti

Ennen lokaalia ajoa täytyy olla elasticsearch pyörimässä. Kontin saa pystyyn kirjautumalla ecr:n ja sitten ajamalla
```shell
aws ecr get-login-password --region eu-west-1 --profile oph-utility | docker login --username AWS --password-stdin 190073735177.dkr.ecr.eu-west-1.amazonaws.com
docker run --rm --name kouta-elastic --env "discovery.type=single-node" -p 127.0.0.1:9200:9200 -p 127.0.0.1:9300:9300 190073735177.dkr.ecr.eu-west-1.amazonaws.com/utility/elasticsearch-kouta:7.17.3
```

Jonkin testiympäristön Elasticsearchia voi hyödyntää helposti seuraavanlaisen ssh-tunnelin avulla:
```
-L9200:konfo.es.hahtuvaopintopolku.fi:80
```

Lokaalin postgres-kannan voi käynnistää dockerilla (sen jälkeen kun kouta-internalin oma kontti-image on buildattu):
```shell
docker run --rm --name koutainternal-db -p 5476:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=ophoph -d koutainternal-postgres
```

Tämän jälkeen käynnistä Ideassa embeddedJettyLauncher.scala (right-click -> Run). Tämä käynnistää samalla
postgresql kontin. Sovellus käynnistyy porttiin 8098 ja Swagger löytyy osoitteesta
`http://localhost:8098/kouta-internal/swagger`.  

### 3.5. Kehitystyökalut

Suositeltava kehitysympäristö on [IntelliJ IDEA](https://www.jetbrains.com/idea/) + 
[scala plugin](https://plugins.jetbrains.com/plugin/1347-scala)

### 3.6. Testidata

Katso kouta-indeksoijan readme:stä kuinka saat lokaaliin elasticsearchiin indeksoitua dataa.
Tämän jälkeen käynnistä kouta-internal tätä lokaalia elasticsearchia vasten.

## 4. Ympäristöt

### 4.1. Testiympäristöt

Testiympäristöjen swaggerit löytyvät seuraavista osoitteista:

- [untuva](https://virkailija.untuvaopintopolku.fi/kouta-internal/swagger)
- [hahtuva](https://virkailija.hahtuvaopintopolku.fi/kouta-internal/swagger)
- [QA eli pallero](https://virkailija.testiopintopolku.fi/kouta-internal/swagger)

### 4.2. Asennus

Asennus hoituu samoilla työkaluilla kuin muidenkin OPH:n palvelujen.
[Cloud-basen dokumentaatiosta](https://github.com/Opetushallitus/cloud-base/tree/master/docs) ja ylläpidolta löytyy apuja.

### 4.3. Lokit

Lokit löytyvät AWS:n CloudWatchista. Log groupin nimemssä on etuliitteenä ympäristön nimi, 
esim. untuva-app-kouta-internal

### 4.4 CI

https://travis-ci.com/github/Opetushallitus/kouta-internal

## 5. Koodin tyyli

Projekti käyttää [Scalafmt](https://scalameta.org/scalafmt/) formatteria ja mavenin 
[Spotless](https://github.com/diffplug/spotless/tree/master/plugin-maven) 
pluginia koodin formatoinnin tarkastamiseen. Spotless ajetaan traviksessa buldin
yhteydessä ja buildi failaa jos spotless check ei mene läpi. Voit
vaihtaa idean scalan code style asetuksista formatteriksi scalafmt ja laittaa vaikka päälle
automaattisen formatoinnin tallennuksen yhteydessä. Spotlessin voi ajaa lokaalisti komennolla
`mvn spotless:check` tai idean maven valikosta