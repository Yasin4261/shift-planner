# Vardiya Planlayıcı (Shift Planner)

Yöneticilerin çalışan vardiyalarını planlaması için geliştirilen modern web uygulaması.

## Teknolojiler

### Backend
- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- H2 Database (Geliştirme için)
- Maven

### Frontend  
- Vue.js 3
- TypeScript
- Vite
- Vue Router
- Pinia (State Management)
- Axios (HTTP Client)

## Özellikler

- **Çalışan Yönetimi**: Çalışan ekleme, düzenleme ve silme
- **Vardiya Planlama**: Esnek vardiya oluşturma ve çakışma kontrolü
- **Haftalık Program**: Görsel haftalık vardiya takvimi
- **Çakışma Kontrolü**: Otomatik vardiya çakışma tespiti
- **Responsive Tasarım**: Mobil ve masaüstü uyumlu arayüz

## Kurulum

### Gereksinimler
- Java 17+
- Node.js 18+
- Maven 3.6+

### Backend Kurulumu

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend http://localhost:8080 adresinde çalışacaktır.

### Frontend Kurulumu

```bash
cd frontend
npm install
npm run dev
```

Frontend http://localhost:5173 adresinde çalışacaktır.

## API Endpoints

### Çalışanlar
- `GET /api/employees` - Tüm çalışanları listele
- `GET /api/employees/active` - Aktif çalışanları listele
- `GET /api/employees/{id}` - Çalışan detayı
- `POST /api/employees` - Yeni çalışan ekle
- `PUT /api/employees/{id}` - Çalışan güncelle
- `DELETE /api/employees/{id}` - Çalışan sil
- `GET /api/employees/search?term={term}` - Çalışan ara

### Vardiyalar
- `GET /api/shifts` - Tüm vardiyaları listele
- `GET /api/shifts/{id}` - Vardiya detayı
- `POST /api/shifts` - Yeni vardiya ekle
- `PUT /api/shifts/{id}` - Vardiya güncelle
- `DELETE /api/shifts/{id}` - Vardiya sil
- `GET /api/shifts/employee/{employeeId}` - Çalışana ait vardiyalar
- `GET /api/shifts/date-range?start={start}&end={end}` - Tarih aralığına göre vardiyalar
- `GET /api/shifts/conflicts?employeeId={id}&startTime={start}&endTime={end}` - Çakışma kontrolü

## Veritabanı

Geliştirme aşamasında H2 in-memory veritabanı kullanılmaktadır. H2 Console'a http://localhost:8080/h2-console adresinden erişebilirsiniz.

**Bağlantı Bilgileri:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (boş)

## Geliştirme

### Backend Geliştirme
```bash
cd backend
mvn spring-boot:run
```

### Frontend Geliştirme
```bash
cd frontend
npm run dev
```

### Build
```bash
# Backend
cd backend
mvn clean package

# Frontend
cd frontend
npm run build
```

## Proje Yapısı

```
shift-planner/
├── backend/                 # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/shiftplanner/
│   │       ├── entity/      # JPA Entities
│   │       ├── repository/  # Data Repositories
│   │       ├── service/     # Business Logic
│   │       ├── controller/  # REST Controllers
│   │       └── config/      # Configuration
│   └── pom.xml
├── frontend/                # Vue.js Frontend
│   ├── src/
│   │   ├── components/      # Vue Components
│   │   ├── views/           # Page Views
│   │   ├── services/        # API Services
│   │   ├── types/           # TypeScript Types
│   │   └── router/          # Vue Router
│   └── package.json
└── README.md
```

## Lisans

MIT License
