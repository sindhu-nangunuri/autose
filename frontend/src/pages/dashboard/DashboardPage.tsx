import React from 'react';
import {
  Box,
  Grid,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  PlayArrow as PlayArrowIcon,
  CheckCircle as CheckCircleIcon,

  Speed as SpeedIcon,
} from '@mui/icons-material';
import { MetricsCard } from '../../components/dashboard/MetricsCard';
import { JobStatusChart } from '../../components/dashboard/JobStatusChart';
import { ActivityTimeline } from '../../components/dashboard/ActivityTimeline';
import { PerformanceChart } from '../../components/dashboard/PerformanceChart';
import { useDashboardMetrics } from '../../hooks/useApi';

export const DashboardPage: React.FC = () => {
  const { data: metrics, isLoading, error } = useDashboardMetrics();

  // Mock data for demonstration when API is not available
  const mockMetrics = {
    totalJobs: 156,
    runningJobs: 8,
    completedJobs: 142,
    failedJobs: 6,
    successRate: 91.0,
    avgExecutionTime: 45.2,
    dataQualityScore: 94.5,
    recentActivity: [
      {
        id: '1',
        type: 'job_completed',
        message: 'Data validation job completed successfully',
        timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
        severity: 'info' as const,
      },
      {
        id: '2',
        type: 'job_started',
        message: 'Customer data orchestration started',
        timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
        severity: 'info' as const,
      },
      {
        id: '3',
        type: 'job_failed',
        message: 'Product catalog validation failed',
        timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
        severity: 'error' as const,
      },
      {
        id: '4',
        type: 'alert_triggered',
        message: 'Data quality threshold warning',
        timestamp: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
        severity: 'warning' as const,
      },
    ],
  };



  const displayMetrics = metrics || mockMetrics;

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          Failed to load dashboard data. Using mock data for demonstration.
        </Alert>
        <DashboardContent metrics={mockMetrics} />
      </Box>
    );
  }

  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '60vh',
        }}
      >
        <CircularProgress size={48} />
      </Box>
    );
  }

  return <DashboardContent metrics={displayMetrics} />;
};

interface DashboardContentProps {
  metrics: any;
}

const DashboardContent: React.FC<DashboardContentProps> = ({ metrics }) => {
  const jobStatusData = [
    { name: 'Completed', value: metrics.completedJobs, color: '#4caf50' },
    { name: 'Running', value: metrics.runningJobs, color: '#2196f3' },
    { name: 'Failed', value: metrics.failedJobs, color: '#f44336' },
    { name: 'Pending', value: 12, color: '#ff9800' },
  ];

  const performanceData = Array.from({ length: 24 }, (_, i) => ({
    timestamp: new Date(Date.now() - (23 - i) * 60 * 60 * 1000).toISOString(),
    value: Math.floor(Math.random() * 20) + 80 + Math.sin(i / 4) * 10,
    label: 'Quality Score',
  }));

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>
        Dashboard Overview
      </Typography>

      {/* Metrics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <MetricsCard
            title="Total Jobs"
            value={metrics.totalJobs}
            subtitle="All time"
            trend="up"
            trendValue="+12%"
            color="primary"
            icon={<DashboardIcon />}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricsCard
            title="Running Jobs"
            value={metrics.runningJobs}
            subtitle="Currently active"
            color="info"
            icon={<PlayArrowIcon />}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricsCard
            title="Success Rate"
            value={`${metrics.successRate}%`}
            subtitle="Last 30 days"
            trend="up"
            trendValue="+2.1%"
            color="success"
            icon={<CheckCircleIcon />}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricsCard
            title="Avg Execution Time"
            value={`${metrics.avgExecutionTime}s`}
            subtitle="Per job"
            trend="down"
            trendValue="-5.2s"
            color="warning"
            icon={<SpeedIcon />}
          />
        </Grid>
      </Grid>

      {/* Charts and Activity */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <JobStatusChart
            data={jobStatusData}
            title="Job Status Distribution"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <ActivityTimeline
            activities={metrics.recentActivity}
            title="Recent Activity"
            maxItems={8}
          />
        </Grid>
        <Grid item xs={12}>
          <PerformanceChart
            data={performanceData}
            title="Data Quality Score Over Time"
            dataKey="value"
          />
        </Grid>
      </Grid>
    </Box>
  );
};