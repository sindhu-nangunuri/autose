// Test script to simulate frontend functionality
const API_BASE_URL = 'http://localhost:8081/api';

async function testFrontendIntegration() {
  console.log('🧪 Testing Frontend Integration...\n');
  
  try {
    // Test 1: Load SharePoint files (like frontend does on page load)
    console.log('1. Loading SharePoint files...');
    const sharepointResponse = await fetch(`${API_BASE_URL}/sharepoint/files`);
    const sharepointData = await sharepointResponse.json();
    console.log('✅ SharePoint files loaded:', sharepointData.files);
    
    // Test 2: Generate sample data (like clicking "Generate Sample Data" button)
    console.log('\n2. Generating sample dataset...');
    const sampleResponse = await fetch(`${API_BASE_URL}/data-quality/sample-dataset`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    });
    const sampleDataset = await sampleResponse.json();
    console.log('✅ Sample dataset generated:', {
      id: sampleDataset.id,
      name: sampleDataset.name,
      rowCount: sampleDataset.rowCount,
      columnCount: sampleDataset.columnCount
    });
    
    // Test 3: Analyze the dataset (like frontend does after generating sample data)
    console.log('\n3. Analyzing dataset...');
    const analysisResponse = await fetch(`${API_BASE_URL}/data-quality/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sampleDataset),
    });
    const analysisReport = await analysisResponse.json();
    
    console.log('✅ Analysis completed:');
    console.log('   Pre-processing score:', analysisReport.preProcessingScore.overallScore.toFixed(3), 'Grade:', analysisReport.preProcessingScore.grade);
    console.log('   Post-processing score:', analysisReport.postProcessingScore.overallScore.toFixed(3), 'Grade:', analysisReport.postProcessingScore.grade);
    console.log('   Results count:', analysisReport.results.length);
    
    // Test 4: Verify chart data structure (like frontend chart rendering)
    console.log('\n4. Testing chart data structure...');
    const chartData = analysisReport.results.map(result => ({
      name: typeof result.metric === 'string' ? result.metric : (result.metric.displayName || result.metric.name),
      score: result.score * 100,
      threshold: result.threshold * 100,
      passed: result.passed,
    }));
    
    console.log('✅ Chart data prepared:');
    chartData.forEach(item => {
      console.log(`   ${item.name}: ${item.score.toFixed(1)}% (${item.passed ? 'PASS' : 'FAIL'})`);
    });
    
    // Test 5: Test SharePoint file analysis
    console.log('\n5. Testing SharePoint file analysis...');
    const fileName = sharepointData.files[0]; // Use first file
    const fileAnalysisResponse = await fetch(`${API_BASE_URL}/sharepoint/analyze/${encodeURIComponent(fileName)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    });
    const fileAnalysisReport = await fileAnalysisResponse.json();
    
    console.log('✅ SharePoint file analysis completed:');
    console.log('   File:', fileName);
    console.log('   Dataset name:', fileAnalysisReport.datasetName);
    console.log('   Post-processing score:', fileAnalysisReport.postProcessingScore.overallScore.toFixed(3), 'Grade:', fileAnalysisReport.postProcessingScore.grade);
    
    console.log('\n🎉 All frontend integration tests passed successfully!');
    console.log('\n📊 Summary:');
    console.log('   ✅ SharePoint file listing');
    console.log('   ✅ Sample data generation');
    console.log('   ✅ Dataset analysis');
    console.log('   ✅ Chart data preparation');
    console.log('   ✅ SharePoint file analysis');
    console.log('\n🚀 Frontend should be fully functional!');
    
  } catch (error) {
    console.error('❌ Frontend integration test failed:', error);
    console.error('Error details:', error.message);
  }
}

// Run the test
testFrontendIntegration();