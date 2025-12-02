/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_VERSION: string
  readonly VITE_ENABLE_REAL_TIME_UPDATES: string
  readonly VITE_ENABLE_NOTIFICATIONS: string
  readonly VITE_ENABLE_ADVANCED_CHARTS: string
  readonly VITE_DASHBOARD_REFRESH_INTERVAL: string
  readonly VITE_JOBS_REFRESH_INTERVAL: string
  readonly VITE_LOGS_REFRESH_INTERVAL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}