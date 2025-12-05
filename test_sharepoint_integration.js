#!/usr/bin/env node

/**
 * Comprehensive SharePoint Integration Test
 * Tests the complete flow from SharePoint file analysis to frontend data processing
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

async function testSharePointEndpoints() {
    console.log('🧪 Testing SharePoint Integration...\n');

    try {
        // Test 1: List available SharePoint files
        console.log('1️⃣ Testing SharePoint file listing...');
        const listResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/sharepoint/files',
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (listResponse.status === 200) {
            console.log('✅ SharePoint file listing successful');
            console.log('📁 Available files:', listResponse.data);
        } else {
            console.log('❌ SharePoint file listing failed:', listResponse.status);
            return false;
        }

        // Test 2: Analyze SharePoint file
        console.log('\n2️⃣ Testing SharePoint file analysis...');
        const analysisResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/sharepoint/analyze/sample-data.xlsx',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (analysisResponse.status === 200) {
            console.log('✅ SharePoint file analysis successful');
            
            const report = analysisResponse.data;
            console.log('📊 Analysis Results:');
            console.log(`   - Dataset: ${report.datasetName}`);
            console.log(`   - Overall Score: ${(report.preProcessingScore.overallScore * 100).toFixed(1)}%`);
            console.log(`   - Grade: ${report.preProcessingScore.grade}`);
            console.log(`   - Processing Time: ${report.processingTimeMs}ms`);
            console.log(`   - Source Type: ${report.metadata?.sourceType}`);
            console.log(`   - Original File: ${report.metadata?.originalFileName}`);
            
            // Test 3: Verify data structure for frontend compatibility
            console.log('\n3️⃣ Testing frontend data structure compatibility...');
            
            // Check required fields for frontend
            const requiredFields = ['id', 'datasetName', 'preProcessingScore', 'postProcessingScore', 'results', 'metadata'];
            const missingFields = requiredFields.filter(field => !(field in report));
            
            if (missingFields.length === 0) {
                console.log('✅ All required fields present for frontend');
            } else {
                console.log('❌ Missing required fields:', missingFields);
                return false;
            }
            
            // Check score structure
            if (report.preProcessingScore && report.preProcessingScore.metricScores) {
                console.log('✅ Score structure compatible with frontend charts');
                console.log('📈 Metrics available:', Object.keys(report.preProcessingScore.metricScores));
            } else {
                console.log('❌ Score structure incompatible with frontend');
                return false;
            }
            
            // Check results structure for table display
            if (Array.isArray(report.results) && report.results.length > 0) {
                console.log('✅ Results structure compatible with frontend table');
                console.log('📋 Result metrics:', report.results.map(r => r.metric));
            } else {
                console.log('❌ Results structure incompatible with frontend');
                return false;
            }
            
            // Test 4: Simulate frontend data processing
            console.log('\n4️⃣ Testing frontend data processing simulation...');
            
            // Simulate chart data preparation (from frontend logic)
            const chartData = Object.entries(report.preProcessingScore.metricScores).map(([metric, score]) => ({
                metric: metric,
                score: score * 100,
                threshold: 90 // Default threshold for display
            }));
            
            console.log('✅ Chart data prepared successfully');
            console.log('📊 Chart data sample:', chartData.slice(0, 2));
            
            // Simulate table data preparation
            const tableData = report.results.map(result => ({
                metric: typeof result.metric === 'string' ? result.metric : result.metric.name || 'Unknown',
                score: (result.score * 100).toFixed(1) + '%',
                threshold: (result.threshold * 100).toFixed(1) + '%',
                status: result.passed ? 'Pass' : 'Fail',
                issues: result.issues.length
            }));
            
            console.log('✅ Table data prepared successfully');
            console.log('📋 Table data sample:', tableData.slice(0, 2));
            
            return true;
            
        } else {
            console.log('❌ SharePoint file analysis failed:', analysisResponse.status);
            if (analysisResponse.data.error) {
                console.log('   Error:', analysisResponse.data.error);
            }
            return false;
        }

    } catch (error) {
        console.log('❌ Test failed with error:', error.message);
        return false;
    }
}

async function testHealthEndpoint() {
    console.log('🏥 Testing backend health...');
    
    try {
        const healthResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/data-quality/health',
            method: 'GET'
        });

        if (healthResponse.status === 200) {
            console.log('✅ Backend is healthy');
            console.log('💚 Health status:', healthResponse.data);
            return true;
        } else {
            console.log('❌ Backend health check failed:', healthResponse.status);
            return false;
        }
    } catch (error) {
        console.log('❌ Backend health check error:', error.message);
        return false;
    }
}

async function runTests() {
    console.log('🚀 Starting SharePoint Integration Tests\n');
    
    const healthOk = await testHealthEndpoint();
    if (!healthOk) {
        console.log('\n❌ Backend health check failed. Stopping tests.');
        process.exit(1);
    }
    
    console.log('');
    const integrationOk = await testSharePointEndpoints();
    
    console.log('\n' + '='.repeat(60));
    if (integrationOk) {
        console.log('🎉 All SharePoint integration tests PASSED!');
        console.log('✅ SharePoint file processing working correctly');
        console.log('✅ Frontend data structure compatibility verified');
        console.log('✅ Ready for full application testing');
    } else {
        console.log('💥 SharePoint integration tests FAILED!');
        console.log('❌ Issues found that need to be resolved');
    }
    console.log('='.repeat(60));
    
    process.exit(integrationOk ? 0 : 1);
}

runTests();