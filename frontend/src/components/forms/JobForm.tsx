import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Box,
  Grid,
  Typography,
  Divider,
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';

import { JobFormData, JobType } from '../../types';

interface JobFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: JobFormData) => void;
  initialData?: Partial<JobFormData>;
  title?: string;
  loading?: boolean;
}



const jobTypes: { value: JobType; label: string }[] = [
  { value: 'data_quality', label: 'Data Quality' },
  { value: 'orchestration', label: 'Orchestration' },
  { value: 'monitoring', label: 'Monitoring' },
  { value: 'validation', label: 'Validation' },
];

const scheduleOptions = [
  { value: '*/5 * * * *', label: 'Every 5 minutes' },
  { value: '0 * * * *', label: 'Hourly' },
  { value: '0 0 * * *', label: 'Daily' },
  { value: '0 0 * * 0', label: 'Weekly' },
  { value: '0 0 1 * *', label: 'Monthly' },
  { value: 'manual', label: 'Manual' },
];

export const JobForm: React.FC<JobFormProps> = ({
  open,
  onClose,
  onSubmit,
  initialData,
  title = 'Create New Job',
  loading = false,
}) => {
  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: {
      name: '',
      description: '',
      type: 'data_quality' as JobType,
      dataSource: '',
      schedule: 'manual',
      validationRules: [] as string[],
      notifications: {
        email: [] as string[],
        slack: '',
        webhook: '',
        onSuccess: false,
        onFailure: true,
        onWarning: true,
      },
      ...initialData,
    },
  });

  const handleFormSubmit = (data: any) => {
    onSubmit(data as JobFormData);
    reset();
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <form onSubmit={handleSubmit(handleFormSubmit)}>
        <DialogContent>
          <Grid container spacing={3}>
            {/* Basic Information */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Basic Information
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Controller
                name="name"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Job Name"
                    fullWidth
                    error={!!errors.name}
                    helperText={errors.name?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="type"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.type}>
                    <InputLabel>Job Type</InputLabel>
                    <Select {...field} label="Job Type">
                      {jobTypes.map((type) => (
                        <MenuItem key={type.value} value={type.value}>
                          {type.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="description"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Description"
                    fullWidth
                    multiline
                    rows={3}
                    error={!!errors.description}
                    helperText={errors.description?.message}
                  />
                )}
              />
            </Grid>

            {/* Configuration */}
            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" gutterBottom>
                Configuration
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="dataSource"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Data Source"
                    fullWidth
                    error={!!errors.dataSource}
                    helperText={errors.dataSource?.message}
                    placeholder="e.g., database://host:port/db"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="schedule"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.schedule}>
                    <InputLabel>Schedule</InputLabel>
                    <Select {...field} label="Schedule">
                      {scheduleOptions.map((option) => (
                        <MenuItem key={option.value} value={option.value}>
                          {option.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            {/* Notifications */}
            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" gutterBottom>
                Notifications
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="notifications.slack"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Slack Webhook URL"
                    fullWidth
                    placeholder="https://hooks.slack.com/..."
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="notifications.webhook"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Custom Webhook URL"
                    fullWidth
                    placeholder="https://your-webhook.com/endpoint"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Controller
                  name="notifications.onSuccess"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notify on Success"
                    />
                  )}
                />
                <Controller
                  name="notifications.onFailure"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notify on Failure"
                    />
                  )}
                />
                <Controller
                  name="notifications.onWarning"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notify on Warning"
                    />
                  )}
                />
              </Box>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? 'Saving...' : 'Save Job'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};