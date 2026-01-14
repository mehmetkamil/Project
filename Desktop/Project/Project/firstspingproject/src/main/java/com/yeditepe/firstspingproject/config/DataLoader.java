package com.yeditepe.firstspingproject.config;

import com.yeditepe.firstspingproject.entity.Event;
import com.yeditepe.firstspingproject.entity.Role;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.RoleRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // ========== ROLLER ==========
            Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                Role role = new Role();
                role.setName("USER");
                role.setDescription("Standard user role");
                roleRepository.save(role);
                System.out.println("✅ USER role created");
                return role;
            });

            Role organizerRole = roleRepository.findByName("ORGANIZER").orElseGet(() -> {
                Role role = new Role();
                role.setName("ORGANIZER");
                role.setDescription("Event organizer role");
                roleRepository.save(role);
                System.out.println("✅ ORGANIZER role created");
                return role;
            });

            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role role = new Role();
                role.setName("ADMIN");
                role.setDescription("Administrator role");
                roleRepository.save(role);
                System.out.println("✅ ADMIN role created");
                return role;
            });

            // ========== KULLANICILAR ==========
            // Admin kullanıcı
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@eventplanner.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.getRoles().add(adminRole);
                admin.getRoles().add(organizerRole);
                userRepository.save(admin);
                System.out.println("✅ Admin user created (admin/admin123)");
            }

            // Organizatör kullanıcı
            User organizer = userRepository.findByUsername("organizer").orElseGet(() -> {
                User user = new User();
                user.setUsername("organizer");
                user.setEmail("organizer@eventplanner.com");
                user.setPassword(passwordEncoder.encode("org123"));
                user.getRoles().add(organizerRole);
                userRepository.save(user);
                System.out.println("✅ Organizer user created (organizer/org123)");
                return user;
            });

            // Demo kullanıcı
            if (userRepository.findByUsername("demo").isEmpty()) {
                User demo = new User();
                demo.setUsername("demo");
                demo.setEmail("demo@eventplanner.com");
                demo.setPassword(passwordEncoder.encode("demo123"));
                demo.getRoles().add(userRole);
                userRepository.save(demo);
                System.out.println("✅ Demo user created (demo/demo123)");
            }

            // ========== ETKİNLİKLER ==========
            if (eventRepository.count() == 0) {
                // Konser 1
                Event concert1 = new Event();
                concert1.setTitle("Tarkan İstanbul Konseri");
                concert1.setDescription("Megastar Tarkan, en sevilen şarkılarıyla unutulmaz bir gece için İstanbul'da! Yıldızların altında efsanevi bir konser deneyimi sizi bekliyor.");
                concert1.setLocation("İstanbul - Harbiye Açıkhava");
                concert1.setCategory("KONSER");
                concert1.setPrice(450.0);
                concert1.setCapacity(5000);
                concert1.setAvailableSeats(4850);
                concert1.setStartDateTime(LocalDateTime.now().plusDays(15).withHour(21).withMinute(0));
                concert1.setEndDateTime(LocalDateTime.now().plusDays(15).withHour(23).withMinute(30));
                concert1.setOrganizer(organizer);
                concert1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(concert1);

                // Konser 2
                Event concert2 = new Event();
                concert2.setTitle("Duman Rock Festivali");
                concert2.setDescription("Türk Rock'ın efsane grubu Duman, en hit parçalarıyla sahne alıyor. Sınır Yok, Bu Akşam, Belki Alışman Lazım ve daha fazlası!");
                concert2.setLocation("Ankara - ODTÜ Vişnelik");
                concert2.setCategory("KONSER");
                concert2.setPrice(350.0);
                concert2.setCapacity(8000);
                concert2.setAvailableSeats(7500);
                concert2.setStartDateTime(LocalDateTime.now().plusDays(22).withHour(20).withMinute(0));
                concert2.setEndDateTime(LocalDateTime.now().plusDays(22).withHour(23).withMinute(0));
                concert2.setOrganizer(organizer);
                concert2.setCreatedAt(LocalDateTime.now());
                eventRepository.save(concert2);

                // Tiyatro 1
                Event theater1 = new Event();
                theater1.setTitle("Hamlet - Shakespeare Klasiği");
                theater1.setDescription("William Shakespeare'in ölümsüz eseri Hamlet, ödüllü oyuncu kadrosuyla sahnede. Olmak ya da olmamak, bütün mesele bu!");
                theater1.setLocation("İstanbul - Zorlu PSM");
                theater1.setCategory("TİYATRO");
                theater1.setPrice(280.0);
                theater1.setCapacity(800);
                theater1.setAvailableSeats(650);
                theater1.setStartDateTime(LocalDateTime.now().plusDays(7).withHour(20).withMinute(30));
                theater1.setEndDateTime(LocalDateTime.now().plusDays(7).withHour(23).withMinute(0));
                theater1.setOrganizer(organizer);
                theater1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(theater1);

                // Tiyatro 2
                Event theater2 = new Event();
                theater2.setTitle("Hisseli Harikalar Kumpanyası");
                theater2.setDescription("Haldun Taner'in muhteşem müzikali, renkli kostümler ve unutulmaz şarkılarla sahneye taşınıyor. Tüm aile için eğlence dolu bir gece!");
                theater2.setLocation("İzmir - Ahmed Adnan Saygun");
                theater2.setCategory("TİYATRO");
                theater2.setPrice(200.0);
                theater2.setCapacity(600);
                theater2.setAvailableSeats(420);
                theater2.setStartDateTime(LocalDateTime.now().plusDays(10).withHour(19).withMinute(0));
                theater2.setEndDateTime(LocalDateTime.now().plusDays(10).withHour(21).withMinute(30));
                theater2.setOrganizer(organizer);
                theater2.setCreatedAt(LocalDateTime.now());
                eventRepository.save(theater2);

                // Spor 1
                Event sport1 = new Event();
                sport1.setTitle("Galatasaray vs Fenerbahçe - Derbi");
                sport1.setDescription("Türk futbolunun en büyük derbisi! Galatasaray ve Fenerbahçe, şampiyonluk yarışında kritik bir maç için karşı karşıya geliyor.");
                sport1.setLocation("İstanbul - Türk Telekom Stadyumu");
                sport1.setCategory("SPOR");
                sport1.setPrice(500.0);
                sport1.setCapacity(52000);
                sport1.setAvailableSeats(8500);
                sport1.setStartDateTime(LocalDateTime.now().plusDays(5).withHour(20).withMinute(0));
                sport1.setEndDateTime(LocalDateTime.now().plusDays(5).withHour(22).withMinute(0));
                sport1.setOrganizer(organizer);
                sport1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(sport1);

                // Spor 2
                Event sport2 = new Event();
                sport2.setTitle("İstanbul Maratonu 2025");
                sport2.setDescription("Kıtalararası maraton! Asya'dan Avrupa'ya koşarak geçeceğiniz eşsiz bir deneyim. Profesyonel ve amatör tüm koşuculara açık.");
                sport2.setLocation("İstanbul - 15 Temmuz Şehitler Köprüsü");
                sport2.setCategory("SPOR");
                sport2.setPrice(150.0);
                sport2.setCapacity(40000);
                sport2.setAvailableSeats(35000);
                sport2.setStartDateTime(LocalDateTime.now().plusDays(45).withHour(9).withMinute(0));
                sport2.setEndDateTime(LocalDateTime.now().plusDays(45).withHour(15).withMinute(0));
                sport2.setOrganizer(organizer);
                sport2.setCreatedAt(LocalDateTime.now());
                eventRepository.save(sport2);

                // Konferans 1
                Event conf1 = new Event();
                conf1.setTitle("Yapay Zeka Zirvesi 2025");
                conf1.setDescription("Türkiye'nin en büyük AI konferansı! Google, Microsoft ve OpenAI'dan uzmanlar, geleceğin teknolojilerini anlatıyor.");
                conf1.setLocation("İstanbul - Haliç Kongre Merkezi");
                conf1.setCategory("KONFERANS");
                conf1.setPrice(750.0);
                conf1.setCapacity(2000);
                conf1.setAvailableSeats(1200);
                conf1.setStartDateTime(LocalDateTime.now().plusDays(30).withHour(9).withMinute(0));
                conf1.setEndDateTime(LocalDateTime.now().plusDays(30).withHour(18).withMinute(0));
                conf1.setOrganizer(organizer);
                conf1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(conf1);

                // Workshop 1
                Event workshop1 = new Event();
                workshop1.setTitle("React & Node.js Bootcamp");
                workshop1.setDescription("3 günlük yoğun eğitim programı! Sıfırdan ileri seviye web geliştirme. Sertifika ve iş garantisi dahil.");
                workshop1.setLocation("Ankara - Bilkent Cyberpark");
                workshop1.setCategory("EĞİTİM");
                workshop1.setPrice(1500.0);
                workshop1.setCapacity(50);
                workshop1.setAvailableSeats(12);
                workshop1.setStartDateTime(LocalDateTime.now().plusDays(14).withHour(10).withMinute(0));
                workshop1.setEndDateTime(LocalDateTime.now().plusDays(16).withHour(17).withMinute(0));
                workshop1.setOrganizer(organizer);
                workshop1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(workshop1);

                // Festival 1
                Event festival1 = new Event();
                festival1.setTitle("Cappadox Müzik Festivali");
                festival1.setDescription("Kapadokya'nın büyülü atmosferinde 3 gün sürecek müzik festivali. Yerli ve yabancı 50+ sanatçı, kamp alanı ve gece balonları!");
                festival1.setLocation("Nevşehir - Kapadokya");
                festival1.setCategory("FESTİVAL");
                festival1.setPrice(800.0);
                festival1.setCapacity(15000);
                festival1.setAvailableSeats(11000);
                festival1.setStartDateTime(LocalDateTime.now().plusDays(60).withHour(12).withMinute(0));
                festival1.setEndDateTime(LocalDateTime.now().plusDays(62).withHour(23).withMinute(59));
                festival1.setOrganizer(organizer);
                festival1.setCreatedAt(LocalDateTime.now());
                eventRepository.save(festival1);

                // Stand-up
                Event standup = new Event();
                standup.setTitle("Cem Yılmaz - Diamond Elite Platinum Plus");
                standup.setDescription("Türkiye'nin en sevilen komedyeni Cem Yılmaz, yepyeni gösterisiyle sahnede! Kahkaha garantili unutulmaz bir gece.");
                standup.setLocation("İstanbul - Volkswagen Arena");
                standup.setCategory("STAND-UP");
                standup.setPrice(600.0);
                standup.setCapacity(4000);
                standup.setAvailableSeats(500);
                standup.setStartDateTime(LocalDateTime.now().plusDays(8).withHour(21).withMinute(0));
                standup.setEndDateTime(LocalDateTime.now().plusDays(8).withHour(23).withMinute(30));
                standup.setOrganizer(organizer);
                standup.setCreatedAt(LocalDateTime.now());
                eventRepository.save(standup);

                // Sergi
                Event exhibition = new Event();
                exhibition.setTitle("Van Gogh: Dijital Sergi");
                exhibition.setDescription("Van Gogh'un eserleri 360° projeksiyon teknolojisiyle canlanıyor. Yıldızlı Gece'nin içinde yürüyün!");
                exhibition.setLocation("İstanbul - Tersane");
                exhibition.setCategory("SERGİ");
                exhibition.setPrice(250.0);
                exhibition.setCapacity(200);
                exhibition.setAvailableSeats(80);
                exhibition.setStartDateTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
                exhibition.setEndDateTime(LocalDateTime.now().plusDays(90).withHour(20).withMinute(0));
                exhibition.setOrganizer(organizer);
                exhibition.setCreatedAt(LocalDateTime.now());
                eventRepository.save(exhibition);

                // Çocuk etkinliği
                Event kids = new Event();
                kids.setTitle("Rafadan Tayfa Müzikali");
                kids.setDescription("Çocukların sevgilisi Rafadan Tayfa karakterleri sahneye çıkıyor! Şarkılar, danslar ve sürprizlerle dolu eğlenceli bir gösteri.");
                kids.setLocation("Ankara - Congresium");
                kids.setCategory("ÇOCUK");
                kids.setPrice(180.0);
                kids.setCapacity(1500);
                kids.setAvailableSeats(900);
                kids.setStartDateTime(LocalDateTime.now().plusDays(12).withHour(14).withMinute(0));
                kids.setEndDateTime(LocalDateTime.now().plusDays(12).withHour(16).withMinute(0));
                kids.setOrganizer(organizer);
                kids.setCreatedAt(LocalDateTime.now());
                eventRepository.save(kids);

                System.out.println("✅ 12 sample events created!");
            }
        };
    }
}
