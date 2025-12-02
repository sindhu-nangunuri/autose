import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Typography,
  Box,
  LinearProgress,
  Tooltip,
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  PlayArrow as PlayArrowIcon,
  Stop as StopIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import { Job, JobStatus } from '../../types';

interface JobTableProps {
  jobs: Job[];
  onStartJob: (id: string) => void;
  onStopJob: (id: string) => void;
  onEditJob: (job: Job) => void;
  onDeleteJob: (id: string) => void;
  onViewJob: (job: Job) => void;
  loading?: boolean;
}

export const JobTable: React.FC<JobTableProps> = ({
  jobs,
  onStartJob,
  onStopJob,
  onEditJob,
  onDeleteJob,
  onViewJob,
  loading = false,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedJob, setSelectedJob] = useState<Job | null>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, job: Job) => {
    setAnchorEl(event.currentTarget);
    setSelectedJob(job);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedJob(null);
  };

  const getStatusColor = (status: JobStatus): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' => {
    switch (status) {
      case 'completed':
        return 'success';
      case 'running':
        return 'info';
      case 'failed':
        return 'error';
      case 'pending':
        return 'warning';
      case 'cancelled':
        return 'default';
      default:
        return 'default';
    }
  };

  const formatDuration = (duration?: number) => {
    if (!duration) return '-';
    const minutes = Math.floor(duration / 60);
    const seconds = duration % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const canStart = (job: Job) => ['pending', 'failed', 'cancelled'].includes(job.status);
  const canStop = (job: Job) => job.status === 'running';

  return (
    <TableContainer component={Paper}>
      {loading && <LinearProgress />}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Progress</TableCell>
            <TableCell>Duration</TableCell>
            <TableCell>Created</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {jobs.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} align="center">
                <Typography variant="body2" color="textSecondary" sx={{ py: 4 }}>
                  No jobs found
                </Typography>
              </TableCell>
            </TableRow>
          ) : (
            jobs.map((job) => (
              <TableRow key={job.id} hover>
                <TableCell>
                  <Box>
                    <Typography variant="body2" fontWeight="medium">
                      {job.name}
                    </Typography>
                    {job.description && (
                      <Typography variant="caption" color="textSecondary">
                        {job.description}
                      </Typography>
                    )}
                  </Box>
                </TableCell>
                <TableCell>
                  <Chip
                    label={job.type.replace('_', ' ')}
                    size="small"
                    variant="outlined"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={job.status}
                    color={getStatusColor(job.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LinearProgress
                      variant="determinate"
                      value={job.progress}
                      sx={{ flexGrow: 1, height: 6, borderRadius: 3 }}
                    />
                    <Typography variant="caption" color="textSecondary">
                      {job.progress}%
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell>
                  {formatDuration(job.duration)}
                </TableCell>
                <TableCell>
                  <Typography variant="caption">
                    {formatDate(job.createdAt)}
                  </Typography>
                </TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    {canStart(job) && (
                      <Tooltip title="Start Job">
                        <IconButton
                          size="small"
                          onClick={() => onStartJob(job.id)}
                          color="primary"
                        >
                          <PlayArrowIcon />
                        </IconButton>
                      </Tooltip>
                    )}
                    {canStop(job) && (
                      <Tooltip title="Stop Job">
                        <IconButton
                          size="small"
                          onClick={() => onStopJob(job.id)}
                          color="error"
                        >
                          <StopIcon />
                        </IconButton>
                      </Tooltip>
                    )}
                    <IconButton
                      size="small"
                      onClick={(e) => handleMenuOpen(e, job)}
                    >
                      <MoreVertIcon />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => {
          if (selectedJob) onViewJob(selectedJob);
          handleMenuClose();
        }}>
          <VisibilityIcon sx={{ mr: 1 }} />
          View Details
        </MenuItem>
        <MenuItem onClick={() => {
          if (selectedJob) onEditJob(selectedJob);
          handleMenuClose();
        }}>
          <EditIcon sx={{ mr: 1 }} />
          Edit
        </MenuItem>
        <MenuItem 
          onClick={() => {
            if (selectedJob) onDeleteJob(selectedJob.id);
            handleMenuClose();
          }}
          sx={{ color: 'error.main' }}
        >
          <DeleteIcon sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>
    </TableContainer>
  );
};