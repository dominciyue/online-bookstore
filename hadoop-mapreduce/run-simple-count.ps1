# E-BookStore Keyword Count Script
# PowerShell implementation of MapReduce-like keyword counting

param()

$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  E-BookStore Keyword Count" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$inputDir = "input"
$outputDir = "output"
$keywordsFile = "src\main\resources\keywords.txt"

# Load keywords
Write-Host "[1/4] Loading keywords..." -ForegroundColor Yellow
$keywords = @()
if (Test-Path $keywordsFile) {
    $lines = Get-Content $keywordsFile -Encoding UTF8
    foreach ($line in $lines) {
        $line = $line.Trim()
        if ($line -and (-not $line.StartsWith("#"))) {
            $keywords += $line
        }
    }
}
Write-Host "      Loaded $($keywords.Count) keywords" -ForegroundColor Green

# Initialize counters
$keywordCounts = @{}
foreach ($kw in $keywords) {
    $keywordCounts[$kw] = 0
}

# Read input files
Write-Host "[2/4] Reading input files..." -ForegroundColor Yellow
$inputFiles = Get-ChildItem "$inputDir\*.txt" -ErrorAction SilentlyContinue
$totalFiles = $inputFiles.Count
$totalLines = 0

foreach ($file in $inputFiles) {
    Write-Host "      Processing: $($file.Name)" -ForegroundColor Gray
    $content = Get-Content $file.FullName -Encoding UTF8
    
    foreach ($line in $content) {
        $trimmedLine = $line.Trim()
        
        # Skip comments and metadata
        if ($trimmedLine.StartsWith("#")) { continue }
        if ($trimmedLine.StartsWith("---")) { continue }
        if ($trimmedLine.StartsWith("Title:")) { continue }
        if ($trimmedLine.StartsWith("Author:")) { continue }
        if ([string]::IsNullOrEmpty($trimmedLine)) { continue }
        
        # Remove Description: prefix
        if ($line.StartsWith("Description:")) {
            $line = $line.Substring(12)
        }
        
        $totalLines++
        
        # Count each keyword
        foreach ($keyword in $keywords) {
            # Check if Chinese keyword (contains CJK characters)
            $isChinese = $keyword -match "[\u4e00-\u9fa5]"
            
            if ($isChinese) {
                # Chinese: direct substring match
                $escapedKw = [regex]::Escape($keyword)
                $matches = [regex]::Matches($line, $escapedKw)
                $count = $matches.Count
            } else {
                # English: word boundary match (case insensitive)
                $pattern = "\b" + [regex]::Escape($keyword) + "\b"
                $matches = [regex]::Matches($line, $pattern, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
                $count = $matches.Count
            }
            
            $keywordCounts[$keyword] += $count
        }
    }
}

Write-Host "      Processed $totalFiles files, $totalLines lines" -ForegroundColor Green

# Filter and sort results
Write-Host "[3/4] Aggregating results..." -ForegroundColor Yellow
$results = $keywordCounts.GetEnumerator() | Where-Object { $_.Value -gt 0 } | Sort-Object Value -Descending
$matchedKeywords = @($results).Count
$totalOccurrences = 0
foreach ($r in $results) {
    $totalOccurrences += $r.Value
}

Write-Host "      Found $matchedKeywords keywords, $totalOccurrences occurrences" -ForegroundColor Green

# Create output directory
Write-Host "[4/4] Writing results..." -ForegroundColor Yellow
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

# Write result file
$outputFile = "$outputDir\part-r-00000"
$resultLines = @()
foreach ($r in $results) {
    $resultLines += "$($r.Key)`t$($r.Value)"
}
$resultLines | Out-File $outputFile -Encoding UTF8

Write-Host "      Results saved to: $outputFile" -ForegroundColor Green

# Display results
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Results (sorted by count)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($result in $results) {
    $kw = $result.Key
    $count = $result.Value
    Write-Host "  $kw : $count" -ForegroundColor White
}

Write-Host ""
Write-Host "  Summary:" -ForegroundColor Yellow
Write-Host "    - Input files: $totalFiles" -ForegroundColor Gray
Write-Host "    - Lines processed: $totalLines" -ForegroundColor Gray
Write-Host "    - Keywords matched: $matchedKeywords" -ForegroundColor Gray
Write-Host "    - Total occurrences: $totalOccurrences" -ForegroundColor Gray
Write-Host ""
