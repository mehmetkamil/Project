# Spring Boot Microservices Event Management System

Bu proje, Spring Boot kullanılarak geliştirilmiş bir etkinlik yönetim sistemidir. Mikroservis mimarisine dayalı olarak tasarlanmıştır.

## Proje Yapısı

### Monolitik Proje
- **firstspingproject**: Ana uygulama servisi

### Mikroservisler
- **user-service**: Kullanıcı yönetimi ve kimlik doğrulama
- **event-service**: Etkinlik yönetimi
- **booking-service**: Bilet rezervasyon sistemi
- **payment-service**: Ödeme işlemleri
- **discovery-server**: Eureka Discovery Server

## Teknolojiler

- Java 17+
- Spring Boot 3.x
- Spring Security
- JWT Authentication
- Spring Data JPA
- PostgreSQL
- Maven
- Eureka Discovery Server

## Kurulum

```bash
# Projeyi klonlayın
git clone <repository-url>

# Mikroservisleri derleyin
cd microservices-parent
mvn clean install

# Her servisi ayrı ayrı çalıştırın
cd discovery-server
mvn spring-boot:run
```

## Özellikler

- ✅ JWT tabanlı kimlik doğrulama
- ✅ Mikroservis mimarisi
- ✅ Service Discovery (Eureka)
- ✅ Güvenlik denetim günlükleri
- ✅ RESTful API
- ✅ Yönetici paneli
