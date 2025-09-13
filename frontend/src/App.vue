<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div id="app">
    <header class="header">
      <div class="container">
        <h1 class="logo">
          <RouterLink to="/">Vardiya Planlayıcı</RouterLink>
        </h1>
        
        <nav class="nav">
          <RouterLink to="/" class="nav-link">Ana Sayfa</RouterLink>
          <RouterLink to="/employees" class="nav-link">Çalışanlar</RouterLink>
          <RouterLink to="/shifts" class="nav-link">Vardiyalar</RouterLink>
          <RouterLink to="/schedule" class="nav-link">Program</RouterLink>
          <RouterLink to="/about" class="nav-link">Hakkında</RouterLink>
        </nav>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 Vardiya Planlayıcı. Tüm hakları saklıdır.</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.logo a {
  color: #007bff;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: #f8f9fa;
  color: #007bff;
}

.nav-link.router-link-active {
  background: #007bff;
  color: white;
}

.main {
  flex: 1;
  background: #f8f9fa;
}

.footer {
  background: #fff;
  border-top: 1px solid #e5e5e5;
  padding: 20px 0;
  margin-top: auto;
}

.footer p {
  margin: 0;
  text-align: center;
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .container {
    flex-direction: column;
    height: auto;
    padding: 16px;
  }
  
  .nav {
    margin-top: 16px;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .nav-link {
    font-size: 14px;
    padding: 6px 10px;
  }
}
</style>
