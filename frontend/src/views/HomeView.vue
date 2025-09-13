<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { employeeService } from '../services/employeeService'
import { shiftService } from '../services/shiftService'

const stats = ref({
  totalEmployees: 0,
  activeEmployees: 0,
  todayShifts: 0,
  totalShifts: 0
})

const loadStats = async () => {
  try {
    const [employees, shifts] = await Promise.all([
      employeeService.getAllEmployees(),
      shiftService.getAllShifts()
    ])

    const today = new Date().toISOString().split('T')[0]
    const todayShifts = shifts.filter(shift => {
      const shiftDate = new Date(shift.startTime).toISOString().split('T')[0]
      return shiftDate === today
    })

    stats.value = {
      totalEmployees: employees.length,
      activeEmployees: employees.filter(emp => emp.active).length,
      todayShifts: todayShifts.length,
      totalShifts: shifts.length
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="home-view">
    <div class="hero">
      <div class="container">
        <h1>Vardiya Planlayıcıya Hoş Geldiniz</h1>
        <p>Çalışanlarınızın vardiyalarını kolayca planlayın ve yönetin.</p>
        <div class="quick-actions">
          <RouterLink to="/employees" class="btn btn-primary">
            Çalışanları Yönet
          </RouterLink>
          <RouterLink to="/shifts" class="btn btn-secondary">
            Vardiya Ekle
          </RouterLink>
          <RouterLink to="/schedule" class="btn btn-success">
            Program Görünümü
          </RouterLink>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <div class="container">
        <h2>Sistem Özeti</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-number">{{ stats.totalEmployees }}</div>
            <div class="stat-label">Toplam Çalışan</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.activeEmployees }}</div>
            <div class="stat-label">Aktif Çalışan</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.todayShifts }}</div>
            <div class="stat-label">Bugünkü Vardiyalar</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.totalShifts }}</div>
            <div class="stat-label">Toplam Vardiya</div>
          </div>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2>Özellikler</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">👥</div>
            <h3>Çalışan Yönetimi</h3>
            <p>Çalışanlarınızı ekleyin, düzenleyin ve bilgilerini yönetin.</p>
            <RouterLink to="/employees" class="feature-link">
              Çalışanlara Git →
            </RouterLink>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">🕒</div>
            <h3>Vardiya Planlama</h3>
            <p>Esnek vardiya sistemleri oluşturun ve çakışmaları önleyin.</p>
            <RouterLink to="/shifts" class="feature-link">
              Vardiyalar →
            </RouterLink>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">📅</div>
            <h3>Haftalık Program</h3>
            <p>Haftalık vardiya programını görsel olarak takip edin.</p>
            <RouterLink to="/schedule" class="feature-link">
              Program →
            </RouterLink>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">⚡</div>
            <h3>Hızlı Erişim</h3>
            <p>Çakışma kontrolü ve otomatik uyarı sistemi.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  min-height: calc(100vh - 120px);
}

.hero {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero h1 {
  font-size: 48px;
  margin-bottom: 16px;
  font-weight: bold;
}

.hero p {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: inline-block;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary {
  background: white;
  color: #007bff;
}

.btn-primary:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(255,255,255,0.1);
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #007bff;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #218838;
  transform: translateY(-2px);
}

.stats-section {
  padding: 60px 0;
  background: white;
}

.stats-section h2 {
  text-align: center;
  margin-bottom: 40px;
  font-size: 32px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat-card {
  text-align: center;
  padding: 32px 24px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.features-section {
  padding: 60px 0;
  background: #f8f9fa;
}

.features-section h2 {
  text-align: center;
  margin-bottom: 40px;
  font-size: 32px;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 32px;
}

.feature-card {
  background: white;
  padding: 32px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  text-align: center;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 24px;
  margin-bottom: 12px;
  color: #333;
}

.feature-card p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.feature-link {
  color: #007bff;
  text-decoration: none;
  font-weight: 600;
}

.feature-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 36px;
  }
  
  .hero p {
    font-size: 18px;
  }
  
  .quick-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 200px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .stat-number {
    font-size: 36px;
  }
}
</style>
