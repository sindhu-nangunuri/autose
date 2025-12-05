// Simple test script to verify API integration
const API_BASE_URL = 'http://localhost:8081/api';

async function testAPI() {
  console.log('Testing API integration...');
  
  try {
    // Test 1: Health check
    console.log('\n1. Testing health endpoint...');
    const healthResponse = await fetch(`${API_BASE_URL}/data-quality/health`);
    const healthData = await healthResponse.json();
    console.log('Health check result:', healthData);
    
    // Test 2: Generate sample dataset
    console.log('\n2. Testing sample dataset generation...');
    const sampleResponse = await fetch(`${API_BASE_URL}/data-quality/sample-dataset`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    
    if (!sampleResponse.ok) {
      throw new Error(`HTTP error! status: ${sampleResponse.status}`);
    }
    
    const sampleData = await sampleResponse.json();
    console.log('Sample dataset generated successfully:');
    console.log('- ID:', sampleData.id);
    console.log('- Name:', sampleData.name);
    console.log('- Columns:', sampleData.columns);
    console.log('- Row count:', sampleData.rowCount);
    
    // Test 3: Analyze the sample dataset
    console.log('\n3. Testing dataset analysis...');
    const analysisResponse = await fetch(`${API_BASE_URL}/data-quality/analyze`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(sampleData),
    });
    
    if (!analysisResponse.ok) {
      throw new Error(`HTTP error! status: ${analysisResponse.status}`);
    }
    
    const analysisData = await analysisResponse.json();
    console.log('Analysis completed successfully:');
    console.log('- Overall score:', analysisData.score?.overallScore);
    console.log('- Grade:', analysisData.score?.grade);
    console.log('- Results count:', analysisData.results?.length);
    
    // Test 4: SharePoint files
    console.log('\n4. Testing SharePoint files endpoint...');
    const sharepointResponse = await fetch(`${API_BASE_URL}/sharepoint/files`);
    const sharepointData = await sharepointResponse.json();
    console.log('SharePoint files result:', sharepointData);
    
    console.log('\n✅ All API tests passed successfully!');
    
  } catch (error) {
    console.error('❌ API test failed:', error);
    console.error('Error details:', error.message);
  }
}

// Run the test
testAPI();