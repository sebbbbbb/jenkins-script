package org.ftv

class Utils {

    // Fonctions iOS

    String cleanProjectCmd(String projectName) {
        def cmd = 'rm -rf ./Build/*; '
        cmd += 'rm -rf ~/Library/Developer/Xcode/DerivedData/' + projectName + '*'
        return cmd
    }

    String gemBundleInstallCmd() {
        return 'rvm 2.3.3 do bundle install'
    }

    String podInstallCmd() {  
        def cmd = 'rvm 2.3.3 do bundle exec pod repo update; '
        cmd += 'rvm 2.3.3 do bundle exec pod install'
        return cmd
    }

    // Fonctions Android

}