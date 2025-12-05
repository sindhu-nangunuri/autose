
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CssBaseline } from '@mui/material';
import { theme } from './theme';
import { Layout } from './components/common/Layout';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { OrchestrationPage } from './pages/orchestration/OrchestrationPage';
import { MonitoringPage } from './pages/monitoring/MonitoringPage';
import { SettingsPage } from './pages/settings/SettingsPage';
import DataQualityAnalysis from './pages/DataQualityAnalysis';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
    },
    mutations: {
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <Layout>
            <Routes>
              <Route path="/" element={<DataQualityAnalysis />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/orchestration" element={<OrchestrationPage />} />
              <Route path="/monitoring" element={<MonitoringPage />} />
              <Route path="/settings" element={<SettingsPage />} />
              <Route path="/data-quality" element={<DataQualityAnalysis />} />
            </Routes>
          </Layout>
        </Router>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;