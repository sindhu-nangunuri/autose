import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardHeader,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Divider,
  Alert,
  Snackbar,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Save as SaveIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';

interface SystemSettings {
  apiBaseUrl: string;
  refreshInterval: number;
  maxRetries: number;
  timeout: number;
  enableNotifications: boolean;
  enableRealTimeUpdates: boolean;
  logLevel: string;
  theme: string;
}

interface ValidationRule {
  id: string;
  name: string;
  type: string;
  description: string;
  condition: string;
  severity: string;
  enabled: boolean;
}

export const SettingsPage: React.FC = () => {
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'warning' | 'info';
  }>({
    open: false,
    message: '',
    severity: 'success',
  });

  const [ruleDialogOpen, setRuleDialogOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<ValidationRule | null>(null);

  const { control, handleSubmit, formState: { errors } } = useForm<SystemSettings>({
    defaultValues: {
      apiBaseUrl: 'http://localhost:8000/api',
      refreshInterval: 30,
      maxRetries: 3,
      timeout: 30,
      enableNotifications: true,
      enableRealTimeUpdates: true,
      logLevel: 'info',
      theme: 'light',
    },
  });

  const [validationRules, setValidationRules] = useState<ValidationRule[]>([
    {
      id: '1',
      name: 'Data Completeness Check',
      type: 'completeness',
      description: 'Ensure all required fields are populated',
      condition: 'NOT NULL AND LENGTH(field) > 0',
      severity: 'high',
      enabled: true,
    },
    {
      id: '2',
      name: 'Email Format Validation',
      type: 'validity',
      description: 'Validate email address format',
      condition: 'REGEXP_LIKE(email, \'^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$\')',
      severity: 'medium',
      enabled: true,
    },
    {
      id: '3',
      name: 'Duplicate Record Check',
      type: 'uniqueness',
      description: 'Check for duplicate records based on key fields',
      condition: 'COUNT(*) = 1 GROUP BY key_field',
      severity: 'critical',
      enabled: false,
    },
  ]);

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSaveSettings = (data: SystemSettings) => {
    // TODO: Implement API call to save settings
    console.log('Saving settings:', data);
    showSnackbar('Settings saved successfully', 'success');
  };

  const handleAddRule = () => {
    setEditingRule(null);
    setRuleDialogOpen(true);
  };

  const handleEditRule = (rule: ValidationRule) => {
    setEditingRule(rule);
    setRuleDialogOpen(true);
  };

  const handleDeleteRule = (id: string) => {
    if (window.confirm('Are you sure you want to delete this validation rule?')) {
      setValidationRules(rules => rules.filter(rule => rule.id !== id));
      showSnackbar('Validation rule deleted', 'success');
    }
  };

  const handleToggleRule = (id: string) => {
    setValidationRules(rules =>
      rules.map(rule =>
        rule.id === id ? { ...rule, enabled: !rule.enabled } : rule
      )
    );
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'critical':
        return 'error';
      case 'high':
        return 'warning';
      case 'medium':
        return 'info';
      case 'low':
        return 'success';
      default:
        return 'default';
    }
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>
        Settings
      </Typography>

      <Grid container spacing={3}>
        {/* System Configuration */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="System Configuration" />
            <CardContent>
              <form onSubmit={handleSubmit(handleSaveSettings)}>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Controller
                      name="apiBaseUrl"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="API Base URL"
                          fullWidth
                          error={!!errors.apiBaseUrl}
                          helperText={errors.apiBaseUrl?.message}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="refreshInterval"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Refresh Interval (seconds)"
                          type="number"
                          fullWidth
                          inputProps={{ min: 5, max: 300 }}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="timeout"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Request Timeout (seconds)"
                          type="number"
                          fullWidth
                          inputProps={{ min: 5, max: 120 }}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="maxRetries"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Max Retries"
                          type="number"
                          fullWidth
                          inputProps={{ min: 0, max: 10 }}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="logLevel"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth>
                          <InputLabel>Log Level</InputLabel>
                          <Select {...field} label="Log Level">
                            <MenuItem value="debug">Debug</MenuItem>
                            <MenuItem value="info">Info</MenuItem>
                            <MenuItem value="warning">Warning</MenuItem>
                            <MenuItem value="error">Error</MenuItem>
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Divider sx={{ my: 2 }} />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="enableNotifications"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Switch {...field} checked={field.value} />}
                          label="Enable Notifications"
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="enableRealTimeUpdates"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Switch {...field} checked={field.value} />}
                          label="Enable Real-time Updates"
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Button
                      type="submit"
                      variant="contained"
                      startIcon={<SaveIcon />}
                      fullWidth
                    >
                      Save Settings
                    </Button>
                  </Grid>
                </Grid>
              </form>
            </CardContent>
          </Card>
        </Grid>

        {/* Validation Rules */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader
              title="Validation Rules"
              action={
                <Button
                  variant="contained"
                  size="small"
                  startIcon={<AddIcon />}
                  onClick={handleAddRule}
                >
                  Add Rule
                </Button>
              }
            />
            <CardContent sx={{ p: 0 }}>
              <List>
                {validationRules.map((rule, index) => (
                  <React.Fragment key={rule.id}>
                    <ListItem>
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Typography variant="body1">{rule.name}</Typography>
                            <Typography
                              variant="caption"
                              sx={{
                                px: 1,
                                py: 0.5,
                                borderRadius: 1,
                                bgcolor: `${getSeverityColor(rule.severity)}.light`,
                                color: `${getSeverityColor(rule.severity)}.dark`,
                              }}
                            >
                              {rule.severity}
                            </Typography>
                          </Box>
                        }
                        secondary={
                          <>
                            <Typography variant="body2" color="textSecondary">
                              {rule.description}
                            </Typography>
                            <Typography variant="caption" color="textSecondary">
                              Type: {rule.type}
                            </Typography>
                          </>
                        }
                      />
                      <ListItemSecondaryAction>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Switch
                            checked={rule.enabled}
                            onChange={() => handleToggleRule(rule.id)}
                            size="small"
                          />
                          <IconButton
                            size="small"
                            onClick={() => handleEditRule(rule)}
                          >
                            <EditIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            onClick={() => handleDeleteRule(rule.id)}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Box>
                      </ListItemSecondaryAction>
                    </ListItem>
                    {index < validationRules.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* API Information */}
        <Grid item xs={12}>
          <Card>
            <CardHeader title="API Information" />
            <CardContent>
              <Alert severity="info" sx={{ mb: 2 }}>
                This frontend application is designed to work with the DQ Orchestration Agent backend.
                Make sure the backend service is running and accessible at the configured API base URL.
              </Alert>
              <Grid container spacing={2}>
                <Grid item xs={12} md={4}>
                  <Typography variant="subtitle2" gutterBottom>
                    Expected Endpoints:
                  </Typography>
                  <List dense>
                    <ListItem>
                      <ListItemText primary="GET /api/dashboard/metrics" />
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="GET /api/jobs" />
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="POST /api/jobs" />
                    </ListItem>
                    <ListItem>
                      <ListItemText primary="GET /api/system/health" />
                    </ListItem>
                  </List>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Typography variant="subtitle2" gutterBottom>
                    Authentication:
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Currently configured for development without authentication.
                    In production, implement proper authentication headers.
                  </Typography>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Typography variant="subtitle2" gutterBottom>
                    CORS Configuration:
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Ensure the backend allows requests from this frontend domain.
                    For development, allow localhost origins.
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Validation Rule Dialog */}
      <Dialog open={ruleDialogOpen} onClose={() => setRuleDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingRule ? 'Edit Validation Rule' : 'Add Validation Rule'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                label="Rule Name"
                fullWidth
                defaultValue={editingRule?.name || ''}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select defaultValue={editingRule?.type || 'completeness'}>
                  <MenuItem value="completeness">Completeness</MenuItem>
                  <MenuItem value="accuracy">Accuracy</MenuItem>
                  <MenuItem value="consistency">Consistency</MenuItem>
                  <MenuItem value="validity">Validity</MenuItem>
                  <MenuItem value="uniqueness">Uniqueness</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Severity</InputLabel>
                <Select defaultValue={editingRule?.severity || 'medium'}>
                  <MenuItem value="low">Low</MenuItem>
                  <MenuItem value="medium">Medium</MenuItem>
                  <MenuItem value="high">High</MenuItem>
                  <MenuItem value="critical">Critical</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={2}
                defaultValue={editingRule?.description || ''}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Condition"
                fullWidth
                multiline
                rows={3}
                defaultValue={editingRule?.condition || ''}
                placeholder="SQL-like condition or expression"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRuleDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setRuleDialogOpen(false)}>
            {editingRule ? 'Update' : 'Add'} Rule
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};