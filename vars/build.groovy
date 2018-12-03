#!/usr/bin/env groovy

def call(params) {

  def utils = new org.ftv.Utils()

  stage("Check params"){
    def mandatoryParams = ["project_name", "simulator", "slack_token", "slack_channel"]
    mandatoryParams.each {
      if ( params[{$it}] == null ) {
        echo "Le paramètre ${it} doit être renseigné dans le tableau params."
        assert false
      }
    }
  }

  stage("Checkout") {
    checkout scm
  }

  stage("Clean") {
    sh utils.cleanProjectCmd(params["project_name"])
  }

  stage("Bundler") {
    sh utils.gemBundleInstallCmd()
  }

  stage("Cocoapods") {
    sh utils.podInstallCmd()
  }

  stage("Test") {
    sh 'set -o pipefail && env NSUnbufferedIO=YES'
    sh "xcodebuild -workspace "+ params["project_name"] + ".xcworkspace \
	      -scheme "+  params["project_name"] " \
	      -configuration Debug \
	      -destination 'platform=" + params["simulator"] + "'\
	      -derivedDataPath './Build/Test_output/' \
	      -enableCodeCoverage YES clean build test"

    sh 'xsltproc -o ./Build/Test_output/report.junit ./scripts/plist_to_junit.xsl ./Build/Test_output/Logs/Test/*.xcresult/TestSummaries.plist'
  }

  stage("SwiftLint") {
    sh 'mkdir ./Build/Swiftlint_output'
    def cmd = "swiftlint lint "
    if ( params["lint_file"] != null ) {
      cmd+= "--config " + params["lint_file"] + " "
    } 
    cmd+= "--reporter checkstyle  > ./Build/Swiftlint_output/report.xml"
    sh cmd
  }

  stage("Archive results") {
    step([$class: 'JUnitResultArchiver', testResults: 'Build/Test_output/report.junit'])
    step([$class: 'CheckStylePublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'Build/Swiftlint_output/report.xml', unHealthy: ''])
  }

  stage("Slack") {
       slackSend(channel: params["slack_channel"], color: "#42f442", message: "Build de test executée avec succès.", token: params["slack_token"])
  }
}