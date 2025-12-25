# E-BookStore Data Export Script
# Export book data from MySQL to text files

param()

# Set console encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  E-BookStore Data Export (PowerShell)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Database configuration
$dbName = "bookstore_db"
$dbUser = "root"
$dbPassword = "Zy050811"

# Output directory
$outputDir = "input"

# Category mapping
$categoryMap = @{
    "cs-classic" = "CS_Classic"
    "web-dev" = "Web_Dev"
    "programming-lang" = "Programming"
    "sci-fi" = "SciFi"
    "novel" = "Novel"
    "literature" = "Literature"
    "business" = "Business"
    "history" = "History"
}

Write-Host "[1/3] Creating output directory..." -ForegroundColor Yellow
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}
Write-Host "      Output directory: $outputDir" -ForegroundColor Green

Write-Host "[2/3] Connecting to database..." -ForegroundColor Yellow

try {
    # Use mysql with proper encoding - output to temp file first
    $tempFile = [System.IO.Path]::GetTempFileName()
    $query = "SELECT id, title, author, category, description FROM books WHERE deleted = 0 AND description IS NOT NULL"
    
    # Run mysql and save output to temp file with UTF-8 encoding
    $process = Start-Process -FilePath "mysql" -ArgumentList "-u", $dbUser, "-p$dbPassword", $dbName, "-e", "`"$query`"", "--default-character-set=utf8mb4", "-N" -RedirectStandardOutput $tempFile -NoNewWindow -Wait -PassThru
    
    if ($process.ExitCode -ne 0) {
        throw "MySQL connection failed"
    }
    
    # Read the temp file with UTF-8 encoding
    $mysqlResult = Get-Content $tempFile -Encoding UTF8 -Raw
    Remove-Item $tempFile -Force
    
    # Parse results and group by category
    $books = @{}
    $lines = $mysqlResult -split "`n"
    
    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        
        $parts = $line -split "`t"
        if ($parts.Count -ge 5) {
            $id = $parts[0]
            $title = $parts[1]
            $author = $parts[2]
            $category = $parts[3]
            $description = $parts[4]
            
            # Normalize category name
            $fileName = $category
            if ($categoryMap.ContainsKey($category)) {
                $fileName = $categoryMap[$category]
            }
            
            if (-not $books.ContainsKey($fileName)) {
                $books[$fileName] = @()
            }
            
            $books[$fileName] += @{
                id = $id
                title = $title
                author = $author
                description = $description
            }
        }
    }
    
    $totalBooks = 0
    foreach ($key in $books.Keys) {
        $totalBooks += $books[$key].Count
    }
    
    Write-Host "      Read $totalBooks books in $($books.Count) categories" -ForegroundColor Green
    
    Write-Host "[3/3] Writing category files..." -ForegroundColor Yellow
    
    foreach ($category in $books.Keys) {
        $filePath = "$outputDir\$category.txt"
        $bookList = $books[$category]
        
        $content = @()
        $content += "# Category: $category"
        $content += "# Total Books: $($bookList.Count)"
        $content += "# =========================================="
        $content += ""
        
        foreach ($book in $bookList) {
            $content += "--- Book ID: $($book.id) ---"
            $content += "Title: $($book.title)"
            $content += "Author: $($book.author)"
            $content += "Description: $($book.description)"
            $content += ""
        }
        
        # Write with UTF-8 BOM encoding
        $utf8WithBom = New-Object System.Text.UTF8Encoding $true
        [System.IO.File]::WriteAllLines($filePath, $content, $utf8WithBom)
        Write-Host "      $category.txt ($($bookList.Count) books)" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  Export Complete!" -ForegroundColor Green
    Write-Host "  - Total books: $totalBooks" -ForegroundColor Gray
    Write-Host "  - File count: $($books.Count)" -ForegroundColor Gray
    Write-Host "  - Output dir: $((Get-Item $outputDir).FullName)" -ForegroundColor Gray
    Write-Host "==========================================" -ForegroundColor Cyan
    
}
catch {
    Write-Host "      Database connection failed: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please ensure:" -ForegroundColor Yellow
    Write-Host "  1. MySQL service is running" -ForegroundColor Gray
    Write-Host "  2. Database connection info is correct" -ForegroundColor Gray
    Write-Host "  3. mysql command is available" -ForegroundColor Gray
    Write-Host ""
    Write-Host "You can use sample data in input directory directly" -ForegroundColor Yellow
}
