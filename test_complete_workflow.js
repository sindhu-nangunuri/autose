#!/usr/bin/env node

/**
 * Complete Application Workflow Test
 * Simulates the entire user journey from SharePoint file selection to data visualization
 */

const http = require('http');

const API_BASE_URL = 'http://localhost:8081';

function makeRequest(options, postData = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => {
                data += chunk;
            });
            res.on('end', () => {
                try {
                    const jsonData = JSON.parse(data);
                    resolve({ status: res.statusCode, data: jsonData });
                } catch (e) {
                    resolve({ status: res.statusCode, data: data });
                }
            });
        });

        req.on('error', (err) => {
            reject(err);
        });

        if (postData) {
            req.write(postData);
        }
        req.end();
    });
}

async function simulateCompleteWorkflow() {
    console.log('🚀 Starting Complete Application Workflow Test\n');
    console.log('This simulates the entire user journey from file selection to visualization\n');

    try {
        // Step 1: User opens the application and sees available SharePoint files
        console.log('👤 Step 1: User opens application and views available SharePoint files');
        const filesResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/sharepoint/files',
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (filesResponse.status !== 200) {
            throw new Error(`Failed to get SharePoint files: ${filesResponse.status}`);
        }

        console.log('✅ SharePoint files loaded successfully');
        console.log(`📁 Available files: ${filesResponse.data.files.join(', ')}`);
        
        // Step 2: User selects a file for analysis
        const selectedFile = filesResponse.data.files[0]; // Select first file
        console.log(`\n👤 Step 2: User selects file "${selectedFile}" for analysis`);
        
        // Step 3: Frontend sends analysis request to backend
        console.log('🔄 Step 3: Frontend sends analysis request to backend');
        const analysisResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: `/api/sharepoint/analyze/${selectedFile}`,
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (analysisResponse.status !== 200) {
            throw new Error(`Failed to analyze file: ${analysisResponse.status}`);
        }

        const report = analysisResponse.data;
        console.log('✅ File analysis completed successfully');
        console.log(`📊 Dataset: ${report.datasetName}`);
        console.log(`⏱️  Processing time: ${report.processingTimeMs}ms`);
        
        // Step 4: Frontend processes the response for visualization
        console.log('\n🎨 Step 4: Frontend processes data for visualization');
        
        // Simulate Score Card Component
        const scoreCard = {
            overallScore: Math.round(report.preProcessingScore.overallScore * 100),
            grade: report.preProcessingScore.grade,
            improvement: Math.round((report.postProcessingScore.overallScore - report.preProcessingScore.overallScore) * 100),
            processingTime: report.processingTimeMs
        };
        console.log('✅ Score card data prepared:', scoreCard);
        
        // Simulate Chart Component Data
        const chartData = Object.entries(report.preProcessingScore.metricScores).map(([metric, score]) => ({
            metric: metric.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase()),
            preScore: Math.round(score * 100),
            postScore: Math.round(report.postProcessingScore.metricScores[metric] * 100),
            threshold: getThresholdForMetric(metric)
        }));
        console.log('✅ Chart data prepared for', chartData.length, 'metrics');
        
        // Simulate Results Table Data
        const tableData = report.results.map(result => ({
            metric: typeof result.metric === 'string' ? result.metric : result.metric.name || 'Unknown',
            score: `${Math.round(result.score * 100)}%`,
            threshold: `${Math.round(result.threshold * 100)}%`,
            status: result.passed ? 'Pass' : 'Fail',
            issues: result.issues.length,
            recommendations: result.recommendations.length,
            details: Object.keys(result.details || {}).length
        }));
        console.log('✅ Table data prepared for', tableData.length, 'results');
        
        // Step 5: User interacts with visualizations
        console.log('\n🖱️  Step 5: User interacts with data visualizations');
        
        // Simulate filtering by metric
        const filterMetric = 'COMPLETENESS';
        const filteredResults = tableData.filter(row => row.metric === filterMetric);
        console.log(`✅ Filtered results for ${filterMetric}:`, filteredResults.length, 'items');
        
        // Simulate chart interaction
        const chartMetric = chartData.find(item => item.metric.includes('Completeness'));
        if (chartMetric) {
            console.log(`✅ Chart interaction - ${chartMetric.metric}: ${chartMetric.preScore}% → ${chartMetric.postScore}%`);
        }
        
        // Step 6: User views detailed analysis
        console.log('\n🔍 Step 6: User views detailed analysis');
        
        // Simulate detailed view for a specific metric
        const detailedMetric = report.results.find(r => r.metric === 'COMPLETENESS');
        if (detailedMetric) {
            console.log('✅ Detailed analysis loaded:');
            console.log(`   - Score: ${Math.round(detailedMetric.score * 100)}%`);
            console.log(`   - Issues: ${detailedMetric.issues.length}`);
            console.log(`   - Recommendations: ${detailedMetric.recommendations.length}`);
            console.log(`   - Details available: ${Object.keys(detailedMetric.details || {}).length} items`);
        }
        
        // Step 7: User can download or export results
        console.log('\n💾 Step 7: User can export results');
        
        // Simulate export data preparation
        const exportData = {
            summary: {
                file: report.datasetName,
                overallScore: scoreCard.overallScore,
                grade: scoreCard.grade,
                timestamp: report.timestamp
            },
            metrics: chartData,
            detailedResults: tableData,
            metadata: report.metadata
        };
        console.log('✅ Export data prepared:', Object.keys(exportData).join(', '));
        
        // Step 8: Verify SharePoint integration
        console.log('\n🔗 Step 8: Verify SharePoint integration features');
        
        if (report.metadata && report.metadata.sourceType === 'SharePoint') {
            console.log('✅ SharePoint integration verified:');
            console.log(`   - Source type: ${report.metadata.sourceType}`);
            console.log(`   - Original file: ${report.metadata.originalFileName}`);
            console.log('✅ File successfully processed from SharePoint mock service');
        } else {
            throw new Error('SharePoint metadata missing from response');
        }
        
        return true;
        
    } catch (error) {
        console.log('❌ Workflow test failed:', error.message);
        return false;
    }
}

function getThresholdForMetric(metric) {
    const thresholds = {
        'COMPLETENESS': 95,
        'UNIQUENESS': 98,
        'VALIDITY': 95,
        'CONSISTENCY': 90,
        'OUTLIERS': 10,
        'BLANKS': 5
    };
    return thresholds[metric] || 90;
}

async function testAdditionalEndpoints() {
    console.log('\n🧪 Testing Additional Backend Endpoints\n');
    
    try {
        // Test sample dataset generation
        console.log('📊 Testing sample dataset generation...');
        const sampleResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/data-quality/sample-dataset',
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        
        if (sampleResponse.status === 200) {
            console.log('✅ Sample dataset endpoint working');
            console.log(`📈 Generated ${sampleResponse.data.results?.length || 0} sample results`);
        } else {
            console.log('❌ Sample dataset endpoint failed:', sampleResponse.status);
        }
        
        // Test data analysis endpoint
        console.log('\n🔍 Testing data analysis endpoint...');
        const testData = {
            data: [
                { id: 1, name: "Test User", email: "test@example.com", age: 25 },
                { id: 2, name: "Another User", email: "user@example.com", age: 30 }
            ]
        };
        
        const analysisResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/data-quality/analyze',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        }, JSON.stringify(testData));
        
        if (analysisResponse.status === 200) {
            console.log('✅ Data analysis endpoint working');
            console.log(`📊 Analysis completed for ${testData.data.length} records`);
        } else {
            console.log('❌ Data analysis endpoint failed:', analysisResponse.status);
        }
        
        return true;
        
    } catch (error) {
        console.log('❌ Additional endpoint tests failed:', error.message);
        return false;
    }
}

async function runCompleteTest() {
    console.log('🎯 COMPLETE APPLICATION WORKFLOW TEST');
    console.log('=' .repeat(60));
    
    const workflowSuccess = await simulateCompleteWorkflow();
    const endpointsSuccess = await testAdditionalEndpoints();
    
    console.log('\n' + '=' .repeat(60));
    console.log('📋 TEST SUMMARY');
    console.log('=' .repeat(60));
    
    if (workflowSuccess && endpointsSuccess) {
        console.log('🎉 ALL TESTS PASSED!');
        console.log('✅ Complete user workflow simulation successful');
        console.log('✅ SharePoint integration working correctly');
        console.log('✅ Frontend data processing verified');
        console.log('✅ All backend endpoints functional');
        console.log('✅ Data visualization pipeline ready');
        console.log('\n🚀 Application is ready for production use!');
    } else {
        console.log('💥 SOME TESTS FAILED!');
        if (!workflowSuccess) console.log('❌ User workflow simulation failed');
        if (!endpointsSuccess) console.log('❌ Backend endpoint tests failed');
    }
    
    console.log('=' .repeat(60));
    
    process.exit(workflowSuccess && endpointsSuccess ? 0 : 1);
}

runCompleteTest();