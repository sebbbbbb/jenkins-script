#!/usr/bin/env groovy

def call(params) {

  stage("Check params"){
    def mandatoryParams = ["project_name", "project_configuration", "slack_token", "slack_channel", "crashlytics_api_key", "crashlytics_secret", "crashlytics_team"]
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
    echo 'Clean Build directory & DerivedData'
    sh 'rm -rf ./Build/*'
    sh 'rm -rf ~/Library/Developer/Xcode/DerivedData/' + params["project_name"] + '*'
    echo 'Clean done'
  }

  // TODO: Refactor
  stage("Bundler") {
    sh 'rvm 2.3.3 do bundle install'
  }

  // TODO: Refactor
  stage("Cocoapods") {
    sh 'rvm 2.3.3 do bundle exec pod update'
    sh 'rvm 2.3.3 do bundle exec pod install'
  }

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

   stage("Archive") {
    
    def currentPath = pwd()

    sh 'set -o pipefail && env NSUnbufferedIO=YES'
    sh "xcodebuild -exportArchive -archivePath " + currentPath + "/build/" + params["project_name"] + ".xcarchive \
           -exportOptionsPlist exportOptions.plist \
           -exportPath " + currentPath + "/build"

   }

   stage("Fabrics") {
       def currentPath = pwd()
       def cmd = "Pods/Crashlytics/submit " +  params["crashlytics_api_key"] + " " + params["crashlytics_secret"] + "\
                -ipaPath " +  currentPath + "/build/" + params["project_name"] + ".ipa \
                -groupAliases " + params["crashlytics_team"] 
       
        if ( params["crashlytics_changelog"] != null ) {
            cmd+= " -notesPath " + params["crashlytics_changelog"]
        } 
        sh cmd
   }

  stage("Slack") {
    slackSend(channel: params["slack_channel"], color: "#42f442", message: "Export sur fabrics beta, terminé", token: params["slack_token"])
  }
}