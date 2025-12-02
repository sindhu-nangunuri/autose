import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Typography,
  Chip,
  Box,
  useTheme,
} from '@mui/material';
import {
  PlayArrow,
  CheckCircle,
  Error,
  Warning,
  Info,
} from '@mui/icons-material';
import { ActivityItem } from '../../types';

interface ActivityTimelineProps {
  activities: ActivityItem[];
  title?: string;
  maxItems?: number;
}

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  activities,
  title = 'Recent Activity',
  maxItems = 10,
}) => {
  const theme = useTheme();

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'job_started':
        return <PlayArrow sx={{ color: theme.palette.info.main }} />;
      case 'job_completed':
        return <CheckCircle sx={{ color: theme.palette.success.main }} />;
      case 'job_failed':
        return <Error sx={{ color: theme.palette.error.main }} />;
      case 'alert_triggered':
        return <Warning sx={{ color: theme.palette.warning.main }} />;
      default:
        return <Info sx={{ color: theme.palette.grey[500] }} />;
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
      default:
        return 'info';
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

    if (diffInMinutes < 1) {
      return 'Just now';
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes}m ago`;
    } else if (diffInMinutes < 1440) {
      const hours = Math.floor(diffInMinutes / 60);
      return `${hours}h ago`;
    } else {
      const days = Math.floor(diffInMinutes / 1440);
      return `${days}d ago`;
    }
  };

  const displayedActivities = activities.slice(0, maxItems);

  return (
    <Card sx={{ height: '100%' }}>
      <CardHeader title={title} />
      <CardContent sx={{ pt: 0 }}>
        {displayedActivities.length === 0 ? (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              minHeight: 200,
              color: 'text.secondary',
            }}
          >
            <Typography variant="body2">No recent activity</Typography>
          </Box>
        ) : (
          <List sx={{ py: 0 }}>
            {displayedActivities.map((activity, index) => (
              <ListItem
                key={activity.id}
                sx={{
                  px: 0,
                  borderBottom: index < displayedActivities.length - 1 ? 1 : 0,
                  borderColor: 'divider',
                }}
              >
                <ListItemIcon sx={{ minWidth: 40 }}>
                  {getActivityIcon(activity.type)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="body2" sx={{ flexGrow: 1 }}>
                        {activity.message}
                      </Typography>
                      <Chip
                        label={getSeverityColor(activity.severity)}
                        size="small"
                        color={getSeverityColor(activity.severity) as any}
                        variant="outlined"
                      />
                    </Box>
                  }
                  secondary={
                    <Typography variant="caption" color="textSecondary">
                      {formatTimestamp(activity.timestamp)}
                    </Typography>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </CardContent>
    </Card>
  );
};