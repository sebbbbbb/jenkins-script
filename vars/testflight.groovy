#!/usr/bin/env groovy

def call(params) {

  stage("Check params"){
    def mandatoryParams = ["project_name", "project_configuration", "slack_token", "slack_channel", "apple_id", "apple_password"]
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


  //TODO: Refacto
  stage("Build") {

    def currentPath = pwd()

    sh 'set -o pipefail && env NSUnbufferedIO=YES'
    def cmd =   'xcodebuild -workspace ' + params["project_name"] + '.xcworkspace \
	      -scheme ' + params["project_name"] + ' \
	      -configuration ' + params["project_configuration"] + ' \
        archive -archivePath ' + currentPath + '/build/' + params["project_name"] + '.xcarchive'
   
    if (params["project_arch"] != null ) {
        cmd+= ' ARCHS="' + params["project_arch"] + '"'
    }  
   
    sh cmd
   }

   //TODO: Refacto
   stage("Archive") {
    def currentPath = pwd()
    sh 'set -o pipefail && env NSUnbufferedIO=YES'
    sh "xcodebuild -exportArchive -archivePath " + currentPath + "/build/" + params["project_name"] + ".xcarchive \
           -exportOptionsPlist exportOptions.plist \
           -exportPath " + currentPath + "/build"

   }

   stage("Upload to testflight") {
      def currentPath = pwd()
      def cmd = "/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Support/altool --upload-app -f \ " + currentPath + " \ "
                 "/build/zouzous-tvos.ipa -u " + params["apple_id"] + " -p " + params["apple_password"]
      sh cmd
   }

  stage("Slack") {
    slackSend(channel: params["slack_channel"], color: "#42f442", message: "Export sur testflight, terminé", token: params["slack_token"])
  }
}