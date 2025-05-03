@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MVNW_REPOURL - repo url base for downloading maven distribution
@REM MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=

@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)

@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=

@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)
@echo Cannot start maven from wrapper >&2 && exit /b 1

@GOTO :EOF

: end batch / begin powershell #>

$ErrorActionPreference = "Stop"

if ($env:MVNW_VERBOSE -eq "true") {
  $VerbosePreference = "Continue"
}

function Write-Verbose-ErrorAction {
  [CmdletBinding(PositionalBinding = $false)]
  param(
    [Parameter(Position = 0, Mandatory = $true)]
    [string]$Message
  )
  # Verbose message goes to verbose stream
  Write-Verbose $Message
  # Error message goes to stderr
  [Console]::Error.WriteLine($Message)
}

$MAVEN_PROJECTBASEDIR = Split-Path -Parent $MyInvocation.MyCommand.Definition

# Validate and get Java home
if ($env:JAVA_HOME) {
  $javaHome = $env:JAVA_HOME
  if (-not (Test-Path $javaHome)) {
    Write-Verbose-ErrorAction "JAVA_HOME is set to an invalid directory: $javaHome"
    exit 1
  }
} else {
  Write-Verbose-ErrorAction "JAVA_HOME not set"
  exit 1
}

# Validate Maven home
$validMavenHome = $false
$mavenHome = ""
if ($env:M2_HOME) {
  $mavenHome = $env:M2_HOME
  if (Test-Path $mavenHome) {
    $validMavenHome = $true
  } else {
    Write-Verbose-ErrorAction "M2_HOME is set to an invalid directory: $mavenHome"
  }
} elseif ($env:MAVEN_HOME) {
  $mavenHome = $env:MAVEN_HOME
  if (Test-Path $mavenHome) {
    $validMavenHome = $true
  } else {
    Write-Verbose-ErrorAction "MAVEN_HOME is set to an invalid directory: $mavenHome"
  }
}

# Get wrapper properties
$wrapperProperties = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
$wrapperUrl = ""
if (Test-Path $wrapperProperties) {
  $wrapperProperties = Get-Content $wrapperProperties -Raw
  foreach ($line in ($wrapperProperties -split "`n")) {
    $line = $line.Trim()
    if ($line.StartsWith("wrapperUrl=")) {
      $wrapperUrl = $line.Substring("wrapperUrl=".Length)
      break
    }
  }
}

# Download and install Maven
if (-not $validMavenHome) {
  $mavenHome = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven"
  if (-not (Test-Path $mavenHome)) {
    if ($wrapperUrl -eq "") {
      Write-Verbose-ErrorAction "No maven URL provided in maven-wrapper.properties"
      exit 1
    }

    Write-Verbose "Downloading Maven from $wrapperUrl"

    $wrapperJarPath = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
    $wrapperDownloaderPath = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/MavenWrapperDownloader.java"

    # Download wrapper jar if needed
    if (-not (Test-Path $wrapperJarPath)) {
      if (Test-Path $wrapperDownloaderPath) {
        # Compile and run downloader
        $javaClass = "MavenWrapperDownloader"
        $javacCmd = "$javaHome/bin/javac.exe"
        $javaCmd = "$javaHome/bin/java.exe"

        & $javacCmd $wrapperDownloaderPath
        if ($LASTEXITCODE -ne 0) {
          Write-Verbose-ErrorAction "Failed to compile MavenWrapperDownloader.java"
          exit 1
        }

        & $javaCmd -cp "$MAVEN_PROJECTBASEDIR/.mvn/wrapper" $javaClass $wrapperUrl $wrapperJarPath
        if ($LASTEXITCODE -ne 0) {
          Write-Verbose-ErrorAction "Failed to download maven-wrapper.jar"
          exit 1
        }
      } else {
        # Use PowerShell to download
        $wrapperJarPath = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
        $wrapperUrl = $wrapperUrl.Replace(' ', '%20')

        $username = $env:MVNW_USERNAME
        $password = $env:MVNW_PASSWORD
        $credentials = ""

        if ($username -and $password) {
          $credentials = "-Credential (New-Object System.Management.Automation.PSCredential ('$username', (ConvertTo-SecureString '$password' -AsPlainText -Force)))"
        }

        $progressPreference = 'SilentlyContinue'
        Invoke-Expression "Invoke-WebRequest -Uri '$wrapperUrl' -OutFile '$wrapperJarPath' $credentials"
        $progressPreference = 'Continue'
      }
    }

    # Extract Maven distribution
    $wrapperJar = New-Object -TypeName System.IO.FileInfo -ArgumentList $wrapperJarPath
    $mavenDistPath = "$mavenHome/bin"

    New-Item -ItemType Directory -Path $mavenDistPath -Force | Out-Null

    $javaCmd = "$javaHome/bin/java.exe"
    & $javaCmd -jar $wrapperJar -d $mavenHome
    if ($LASTEXITCODE -ne 0) {
      Write-Verbose-ErrorAction "Failed to extract Maven distribution"
      exit 1
    }
  }
}

# Run Maven command
$mavenCmd = "$mavenHome/bin/mvn.cmd"
if (-not (Test-Path $mavenCmd)) {
  Write-Verbose-ErrorAction "Maven command not found: $mavenCmd"
  exit 1
}

Write-Output "MVN_CMD=$mavenCmd"
