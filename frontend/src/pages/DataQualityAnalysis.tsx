import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  LinearProgress,
  Alert,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  CloudUpload as CloudUploadIcon,
  Assessment as AssessmentIcon,
  DataObject as DataObjectIcon,
} from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { apiClient } from '../services/api';
import { Dataset, DataQualityReport, DataQualityResult, DataQualityScore } from '../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const DataQualityAnalysis: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [report, setReport] = useState<DataQualityReport | null>(null);
  const [sharePointFiles, setSharePointFiles] = useState<string[]>([]);
  const [selectedFile, setSelectedFile] = useState<string>('');

  useEffect(() => {
    loadSharePointFiles();
  }, []);

  const loadSharePointFiles = async () => {
    try {
      const response = await apiClient.listSharePointFiles();
      setSharePointFiles(response.files);
    } catch (err) {
      console.error('Error loading SharePoint files:', err);
      setError('Failed to load SharePoint files');
    }
  };

  const handleAnalyzeSharePointFile = async (fileName: string) => {
    setLoading(true);
    setError(null);
    try {
      const analysisReport = await apiClient.analyzeSharePointFile(fileName);
      setReport(analysisReport);
      setSelectedFile(fileName);
      setTabValue(1); // Switch to results tab
    } catch (err) {
      console.error('Error analyzing SharePoint file:', err);
      setError(`Failed to analyze file: ${fileName}`);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateSampleData = async () => {
    setLoading(true);
    setError(null);
    try {
      const sampleDataset = await apiClient.generateSampleDataset();
      setDataset(sampleDataset);
      
      // Analyze the sample dataset
      const analysisReport = await apiClient.analyzeDataset(sampleDataset);
      setReport(analysisReport);
      setSelectedFile('Sample Dataset');
      setTabValue(1); // Switch to results tab
    } catch (err) {
      console.error('Error generating sample data:', err);
      setError('Failed to generate sample data');
    } finally {
      setLoading(false);
    }
  };

  const getScoreColor = (score: number): string => {
    if (score >= 0.8) return '#4caf50'; // Green
    if (score >= 0.6) return '#ff9800'; // Orange
    return '#f44336'; // Red
  };

  const getGradeColor = (grade: string): string => {
    switch (grade) {
      case 'A': return '#4caf50';
      case 'B': return '#8bc34a';
      case 'C': return '#ff9800';
      case 'D': return '#ff5722';
      case 'F': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  const renderScoreCard = (title: string, score: DataQualityScore) => (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          {title}
        </Typography>
        <Box display="flex" alignItems="center" mb={2}>
          <Box flexGrow={1}>
            <Typography variant="h3" color={getScoreColor(score.overallScore)}>
              {(score.overallScore * 100).toFixed(1)}%
            </Typography>
          </Box>
          <Chip
            label={`Grade ${score.grade}`}
            style={{
              backgroundColor: getGradeColor(score.grade),
              color: 'white',
              fontWeight: 'bold'
            }}
          />
        </Box>
        <LinearProgress
          variant="determinate"
          value={score.overallScore * 100}
          sx={{
            height: 8,
            borderRadius: 4,
            backgroundColor: '#e0e0e0',
            '& .MuiLinearProgress-bar': {
              backgroundColor: getScoreColor(score.overallScore),
            },
          }}
        />
      </CardContent>
    </Card>
  );

  const renderMetricsChart = () => {
    if (!report?.results) return null;

    const chartData = report.results.map(result => ({
      name: result.metric.displayName || result.metric.name,
      score: result.score * 100,
      threshold: result.threshold * 100,
      passed: result.passed,
    }));

    return (
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
          <YAxis domain={[0, 100]} />
          <Tooltip formatter={(value: number) => [`${value.toFixed(1)}%`, 'Score']} />
          <Bar dataKey="score" fill="#2196f3" />
          <Bar dataKey="threshold" fill="#ff9800" opacity={0.3} />
        </BarChart>
      </ResponsiveContainer>
    );
  };

  const renderResultsTable = () => {
    if (!report?.results) return null;

    return (
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Metric</TableCell>
              <TableCell align="right">Score</TableCell>
              <TableCell align="right">Threshold</TableCell>
              <TableCell align="center">Status</TableCell>
              <TableCell>Issues</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {report.results.map((result, index) => (
              <TableRow key={index}>
                <TableCell component="th" scope="row">
                  {result.metric.displayName || result.metric.name}
                </TableCell>
                <TableCell align="right">
                  <Typography color={getScoreColor(result.score)}>
                    {(result.score * 100).toFixed(1)}%
                  </Typography>
                </TableCell>
                <TableCell align="right">
                  {(result.threshold * 100).toFixed(1)}%
                </TableCell>
                <TableCell align="center">
                  {result.passed ? (
                    <CheckCircleIcon color="success" />
                  ) : (
                    <ErrorIcon color="error" />
                  )}
                </TableCell>
                <TableCell>
                  {result.issues.length > 0 ? (
                    <Typography variant="body2" color="error">
                      {result.issues.length} issue(s)
                    </Typography>
                  ) : (
                    <Typography variant="body2" color="success">
                      No issues
                    </Typography>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  };

  return (
    <Container maxWidth="xl">
      <Box py={3}>
        <Typography variant="h4" gutterBottom>
          Data Quality Analysis
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab label="Data Sources" icon={<CloudUploadIcon />} />
            <Tab label="Analysis Results" icon={<AssessmentIcon />} disabled={!report} />
            <Tab label="Dataset Details" icon={<DataObjectIcon />} disabled={!dataset && !report} />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    SharePoint Files
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Select a file from SharePoint to analyze
                  </Typography>
                  {sharePointFiles.length > 0 ? (
                    <List>
                      {sharePointFiles.map((file, index) => (
                        <ListItem key={index}>
                          <ListItemIcon>
                            <DataObjectIcon />
                          </ListItemIcon>
                          <ListItemText primary={file} />
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={() => handleAnalyzeSharePointFile(file)}
                            disabled={loading}
                          >
                            Analyze
                          </Button>
                        </ListItem>
                      ))}
                    </List>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      No files available
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Sample Data
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Generate and analyze sample data for testing
                  </Typography>
                  <Button
                    variant="contained"
                    onClick={handleGenerateSampleData}
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <AssessmentIcon />}
                  >
                    {loading ? 'Generating...' : 'Generate & Analyze Sample Data'}
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          {report && (
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h5" gutterBottom>
                  Analysis Results for: {selectedFile}
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                {renderScoreCard('Pre-Processing Score', report.preProcessingScore)}
              </Grid>

              <Grid item xs={12} md={6}>
                {renderScoreCard('Post-Processing Score', report.postProcessingScore)}
              </Grid>

              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Metrics Overview
                    </Typography>
                    {renderMetricsChart()}
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Card>
                  <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Detailed Results
                  </Typography>
                  {renderResultsTable()}
                  </CardContent>
                </Card>
              </Grid>

              {report.rectificationActions && report.rectificationActions.length > 0 && (
                <Grid item xs={12}>
                  <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="h6">
                        Rectification Actions ({report.rectificationActions.length})
                      </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <List>
                        {report.rectificationActions.map((action, index) => (
                          <ListItem key={index}>
                            <ListItemIcon>
                              <CheckCircleIcon color="success" />
                            </ListItemIcon>
                            <ListItemText primary={action} />
                          </ListItem>
                        ))}
                      </List>
                    </AccordionDetails>
                  </Accordion>
                </Grid>
              )}

              {report.summary && (
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        AI Summary
                      </Typography>
                      <Typography variant="body1">
                        {report.summary}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              )}

              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Processing Information
                    </Typography>
                    <Grid container spacing={2}>
                      <Grid item xs={6} md={3}>
                        <Typography variant="body2" color="text.secondary">
                          Processing Time
                        </Typography>
                        <Typography variant="body1">
                          {report.processingTimeMs}ms
                        </Typography>
                      </Grid>
                      <Grid item xs={6} md={3}>
                        <Typography variant="body2" color="text.secondary">
                          Timestamp
                        </Typography>
                        <Typography variant="body1">
                          {new Date(report.timestamp).toLocaleString()}
                        </Typography>
                      </Grid>
                      <Grid item xs={6} md={3}>
                        <Typography variant="body2" color="text.secondary">
                          Report ID
                        </Typography>
                        <Typography variant="body1" sx={{ fontFamily: 'monospace' }}>
                          {report.id}
                        </Typography>
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          {(dataset || report) && (
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h5" gutterBottom>
                  Dataset Details
                </Typography>
              </Grid>

              <Grid item xs={12} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Basic Information
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Dataset Name
                    </Typography>
                    <Typography variant="body1" gutterBottom>
                      {dataset?.name || report?.datasetName}
                    </Typography>
                    
                    <Typography variant="body2" color="text.secondary">
                      Rows
                    </Typography>
                    <Typography variant="body1" gutterBottom>
                      {dataset?.rowCount || 'N/A'}
                    </Typography>
                    
                    <Typography variant="body2" color="text.secondary">
                      Columns
                    </Typography>
                    <Typography variant="body1">
                      {dataset?.columnCount || 'N/A'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              {dataset?.columns && (
                <Grid item xs={12} md={8}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Columns
                      </Typography>
                      <Box display="flex" flexWrap="wrap" gap={1}>
                        {dataset.columns.map((column, index) => (
                          <Chip key={index} label={column} variant="outlined" />
                        ))}
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              )}

              {dataset?.data && dataset.data.length > 0 && (
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Sample Data (First 10 rows)
                      </Typography>
                      <TableContainer>
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              {dataset.columns.map((column, index) => (
                                <TableCell key={index}>{column}</TableCell>
                              ))}
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {dataset.data.slice(0, 10).map((row, index) => (
                              <TableRow key={index}>
                                {dataset.columns.map((column, colIndex) => (
                                  <TableCell key={colIndex}>
                                    {row[column] !== null && row[column] !== undefined 
                                      ? String(row[column]) 
                                      : <em>null</em>
                                    }
                                  </TableCell>
                                ))}
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </CardContent>
                  </Card>
                </Grid>
              )}
            </Grid>
          )}
        </TabPanel>
      </Box>
    </Container>
  );
};

export default DataQualityAnalysis;