import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Grid,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Snackbar,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  FilterList as FilterListIcon,
} from '@mui/icons-material';
import { JobTable } from '../../components/orchestration/JobTable';
import { JobForm } from '../../components/forms/JobForm';
import { 
  useJobs, 
  useCreateJob, 
  useUpdateJob, 
  useDeleteJob, 
  useStartJob, 
  useStopJob 
} from '../../hooks/useApi';
import { Job, JobFormData } from '../../types';

export const OrchestrationPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [jobFormOpen, setJobFormOpen] = useState(false);
  const [editingJob, setEditingJob] = useState<Job | null>(null);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'warning' | 'info';
  }>({
    open: false,
    message: '',
    severity: 'success',
  });

  // API hooks
  const { data: jobs = [], isLoading, error } = useJobs({
    status: statusFilter || undefined,
    type: typeFilter || undefined,
  });

  const createJobMutation = useCreateJob();
  const updateJobMutation = useUpdateJob();
  const deleteJobMutation = useDeleteJob();
  const startJobMutation = useStartJob();
  const stopJobMutation = useStopJob();

  // Mock data for demonstration
  const mockJobs: Job[] = [
    {
      id: '1',
      name: 'Customer Data Validation',
      description: 'Validate customer data quality and completeness',
      status: 'running',
      type: 'data_quality',
      createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date().toISOString(),
      startTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      progress: 65,
      duration: 1800,
      logs: [],
      configuration: {
        dataSource: 'postgresql://localhost:5432/customers',
        schedule: '0 */6 * * *',
      },
    },
    {
      id: '2',
      name: 'Product Catalog Sync',
      description: 'Synchronize product catalog across systems',
      status: 'completed',
      type: 'orchestration',
      createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
      startTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      endTime: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
      progress: 100,
      duration: 3600,
      logs: [],
      configuration: {
        dataSource: 'api://product-service/v1',
        schedule: '0 2 * * *',
      },
    },
    {
      id: '3',
      name: 'Order Processing Monitor',
      description: 'Monitor order processing pipeline',
      status: 'failed',
      type: 'monitoring',
      createdAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      startTime: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
      endTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      progress: 45,
      duration: 900,
      logs: [],
      configuration: {
        dataSource: 'kafka://orders-topic',
        schedule: 'manual',
      },
    },
  ];

  const displayJobs = error ? mockJobs : jobs;

  // Filter jobs based on search term
  const filteredJobs = displayJobs.filter((job) =>
    job.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    job.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCreateJob = async (data: JobFormData) => {
    try {
      await createJobMutation.mutateAsync(data as any);
      setJobFormOpen(false);
      showSnackbar('Job created successfully', 'success');
    } catch (error) {
      showSnackbar('Failed to create job', 'error');
    }
  };

  const handleUpdateJob = async (data: JobFormData) => {
    if (!editingJob) return;
    
    try {
      await updateJobMutation.mutateAsync({
        id: editingJob.id,
        data: data as any,
      });
      setEditingJob(null);
      setJobFormOpen(false);
      showSnackbar('Job updated successfully', 'success');
    } catch (error) {
      showSnackbar('Failed to update job', 'error');
    }
  };

  const handleStartJob = async (id: string) => {
    try {
      await startJobMutation.mutateAsync(id);
      showSnackbar('Job started successfully', 'success');
    } catch (error) {
      showSnackbar('Failed to start job', 'error');
    }
  };

  const handleStopJob = async (id: string) => {
    try {
      await stopJobMutation.mutateAsync(id);
      showSnackbar('Job stopped successfully', 'success');
    } catch (error) {
      showSnackbar('Failed to stop job', 'error');
    }
  };

  const handleEditJob = (job: Job) => {
    setEditingJob(job);
    setJobFormOpen(true);
  };

  const handleDeleteJob = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this job?')) {
      try {
        await deleteJobMutation.mutateAsync(id);
        showSnackbar('Job deleted successfully', 'success');
      } catch (error) {
        showSnackbar('Failed to delete job', 'error');
      }
    }
  };

  const handleViewJob = (job: Job) => {
    // TODO: Implement job details view
    console.log('View job:', job);
  };

  const handleCloseJobForm = () => {
    setJobFormOpen(false);
    setEditingJob(null);
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Job Orchestration
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setJobFormOpen(true)}
        >
          Create Job
        </Button>
      </Box>

      {error && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          Unable to connect to API. Showing mock data for demonstration.
        </Alert>
      )}

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                placeholder="Search jobs..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={statusFilter}
                  label="Status"
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  <MenuItem value="pending">Pending</MenuItem>
                  <MenuItem value="running">Running</MenuItem>
                  <MenuItem value="completed">Completed</MenuItem>
                  <MenuItem value="failed">Failed</MenuItem>
                  <MenuItem value="cancelled">Cancelled</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  value={typeFilter}
                  label="Type"
                  onChange={(e) => setTypeFilter(e.target.value)}
                >
                  <MenuItem value="">All Types</MenuItem>
                  <MenuItem value="data_quality">Data Quality</MenuItem>
                  <MenuItem value="orchestration">Orchestration</MenuItem>
                  <MenuItem value="monitoring">Monitoring</MenuItem>
                  <MenuItem value="validation">Validation</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<FilterListIcon />}
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('');
                  setTypeFilter('');
                }}
              >
                Clear
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Jobs Table */}
      <JobTable
        jobs={filteredJobs}
        onStartJob={handleStartJob}
        onStopJob={handleStopJob}
        onEditJob={handleEditJob}
        onDeleteJob={handleDeleteJob}
        onViewJob={handleViewJob}
        loading={isLoading}
      />

      {/* Job Form Dialog */}
      <JobForm
        open={jobFormOpen}
        onClose={handleCloseJobForm}
        onSubmit={editingJob ? handleUpdateJob : handleCreateJob}
        initialData={editingJob ? {
          name: editingJob.name,
          description: editingJob.description,
          type: editingJob.type,
          dataSource: editingJob.configuration.dataSource || '',
          schedule: editingJob.configuration.schedule || 'manual',
          validationRules: [],
          notifications: editingJob.configuration.notifications || {
            email: [],
            slack: '',
            webhook: '',
            onSuccess: false,
            onFailure: true,
            onWarning: true,
          },
        } : undefined}
        title={editingJob ? 'Edit Job' : 'Create New Job'}
        loading={createJobMutation.isPending || updateJobMutation.isPending}
      />

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