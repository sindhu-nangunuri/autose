import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardHeader,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  CheckCircle as CheckCircleIcon,

} from '@mui/icons-material';
import { PerformanceChart } from '../../components/dashboard/PerformanceChart';
import { useSystemHealth, useSystemMetrics } from '../../hooks/useApi';

interface LogEntry {
  id: string;
  timestamp: string;
  level: 'error' | 'warning' | 'info' | 'debug';
  message: string;
  source: string;
  details?: string;
}

interface SystemAlert {
  id: string;
  type: 'error' | 'warning' | 'info';
  title: string;
  message: string;
  timestamp: string;
  resolved: boolean;
}

export const MonitoringPage: React.FC = () => {
  const [timeRange, setTimeRange] = useState('24h');
  const [logLevel, setLogLevel] = useState('all');

  const { data: systemHealth, error: healthError } = useSystemHealth();
  const { error: metricsError } = useSystemMetrics();

  // Mock data for demonstration
  const mockSystemHealth = {
    status: 'healthy',
    uptime: '5d 12h 34m',
    version: '1.2.3',
    lastCheck: new Date().toISOString(),
    services: [
      { name: 'API Server', status: 'healthy', responseTime: 45 },
      { name: 'Database', status: 'healthy', responseTime: 12 },
      { name: 'Message Queue', status: 'warning', responseTime: 156 },
      { name: 'File Storage', status: 'healthy', responseTime: 23 },
    ],
  };

  const mockLogs: LogEntry[] = [
    {
      id: '1',
      timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
      level: 'error',
      message: 'Failed to connect to external API',
      source: 'orchestration-service',
      details: 'Connection timeout after 30 seconds',
    },
    {
      id: '2',
      timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
      level: 'warning',
      message: 'High memory usage detected',
      source: 'system-monitor',
      details: 'Memory usage: 85%',
    },
    {
      id: '3',
      timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      level: 'info',
      message: 'Job completed successfully',
      source: 'job-executor',
      details: 'Job ID: customer-validation-001',
    },
    {
      id: '4',
      timestamp: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
      level: 'info',
      message: 'New job scheduled',
      source: 'scheduler',
      details: 'Job: product-sync, Next run: 2024-01-15 02:00:00',
    },
  ];

  const mockAlerts: SystemAlert[] = [
    {
      id: '1',
      type: 'warning',
      title: 'High Memory Usage',
      message: 'System memory usage is above 80%',
      timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      resolved: false,
    },
    {
      id: '2',
      type: 'error',
      title: 'Service Unavailable',
      message: 'External API service is not responding',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      resolved: true,
    },
  ];

  const mockPerformanceData = Array.from({ length: 24 }, (_, i) => ({
    timestamp: new Date(Date.now() - (23 - i) * 60 * 60 * 1000).toISOString(),
    value: Math.floor(Math.random() * 30) + 70 + Math.sin(i / 4) * 15,
    label: 'CPU Usage %',
  }));

  const displayHealth = healthError ? mockSystemHealth : systemHealth;
  const displayLogs = mockLogs.filter(log => 
    logLevel === 'all' || log.level === logLevel
  );

  const getLogIcon = (level: string) => {
    switch (level) {
      case 'error':
        return <ErrorIcon color="error" />;
      case 'warning':
        return <WarningIcon color="warning" />;
      case 'info':
        return <InfoIcon color="info" />;
      default:
        return <InfoIcon />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy':
        return 'success';
      case 'warning':
        return 'warning';
      case 'error':
        return 'error';
      default:
        return 'default';
    }
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>
        System Monitoring
      </Typography>

      {(healthError || metricsError) && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          Unable to connect to monitoring API. Showing mock data for demonstration.
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* System Health Overview */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="System Health" />
            <CardContent>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="textSecondary">
                  Overall Status
                </Typography>
                <Chip
                  label={displayHealth?.status || 'Unknown'}
                  color={getStatusColor(displayHealth?.status || 'unknown') as any}
                  sx={{ mt: 1 }}
                />
              </Box>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="textSecondary">
                  Uptime: {displayHealth?.uptime || 'Unknown'}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Version: {displayHealth?.version || 'Unknown'}
                </Typography>
              </Box>
              <Typography variant="subtitle2" gutterBottom>
                Services
              </Typography>
              <List dense>
                {displayHealth?.services?.map((service: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemIcon>
                      {service.status === 'healthy' ? (
                        <CheckCircleIcon color="success" />
                      ) : (
                        <WarningIcon color="warning" />
                      )}
                    </ListItemIcon>
                    <ListItemText
                      primary={service.name}
                      secondary={`Response time: ${service.responseTime}ms`}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* Active Alerts */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Active Alerts" />
            <CardContent>
              {mockAlerts.filter(alert => !alert.resolved).length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <CheckCircleIcon color="success" sx={{ fontSize: 48, mb: 1 }} />
                  <Typography variant="body2" color="textSecondary">
                    No active alerts
                  </Typography>
                </Box>
              ) : (
                <List>
                  {mockAlerts
                    .filter(alert => !alert.resolved)
                    .map((alert) => (
                      <ListItem key={alert.id}>
                        <ListItemIcon>
                          {alert.type === 'error' ? (
                            <ErrorIcon color="error" />
                          ) : (
                            <WarningIcon color="warning" />
                          )}
                        </ListItemIcon>
                        <ListItemText
                          primary={alert.title}
                          secondary={
                            <>
                              <Typography variant="body2">
                                {alert.message}
                              </Typography>
                              <Typography variant="caption" color="textSecondary">
                                {formatTimestamp(alert.timestamp)}
                              </Typography>
                            </>
                          }
                        />
                      </ListItem>
                    ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Performance Chart */}
        <Grid item xs={12}>
          <PerformanceChart
            data={mockPerformanceData}
            title="System Performance"
            dataKey="value"
          />
        </Grid>

        {/* System Logs */}
        <Grid item xs={12}>
          <Card>
            <CardHeader
              title="System Logs"
              action={
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <FormControl size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Time Range</InputLabel>
                    <Select
                      value={timeRange}
                      label="Time Range"
                      onChange={(e) => setTimeRange(e.target.value)}
                    >
                      <MenuItem value="1h">Last Hour</MenuItem>
                      <MenuItem value="24h">Last 24 Hours</MenuItem>
                      <MenuItem value="7d">Last 7 Days</MenuItem>
                      <MenuItem value="30d">Last 30 Days</MenuItem>
                    </Select>
                  </FormControl>
                  <FormControl size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Log Level</InputLabel>
                    <Select
                      value={logLevel}
                      label="Log Level"
                      onChange={(e) => setLogLevel(e.target.value)}
                    >
                      <MenuItem value="all">All Levels</MenuItem>
                      <MenuItem value="error">Error</MenuItem>
                      <MenuItem value="warning">Warning</MenuItem>
                      <MenuItem value="info">Info</MenuItem>
                      <MenuItem value="debug">Debug</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              }
            />
            <CardContent sx={{ p: 0 }}>
              <TableContainer component={Paper} elevation={0}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Level</TableCell>
                      <TableCell>Timestamp</TableCell>
                      <TableCell>Source</TableCell>
                      <TableCell>Message</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {displayLogs.map((log) => (
                      <TableRow key={log.id} hover>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {getLogIcon(log.level)}
                            <Typography variant="caption" sx={{ textTransform: 'uppercase' }}>
                              {log.level}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {formatTimestamp(log.timestamp)}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={log.source} size="small" variant="outlined" />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {log.message}
                          </Typography>
                          {log.details && (
                            <Typography variant="caption" color="textSecondary">
                              {log.details}
                            </Typography>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};