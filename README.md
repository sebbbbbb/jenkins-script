# JenkinsScript

## Présentation

Ce repo peut être utilisé en tant que shared librarie sur Jenkins pour mutualiser les scripts entre utilisateurs.
A l'heure actuelle il contient deux script iOS :

###  Build

Permet de lancer un build avec les tests unitaires.
Paramètre :
* project_name
* simulator : simu utilisé pour les TU
* slack_token
* lint_file : optionnel, permet de spécifier un fichier custom pour SwiftLint

Exemple d'utilisation :
```
// Dans un Jenkinsfile
build(
[
"project_name": "monprojet",
"simulator": "tvOS Simulator,name=Apple TV 4K",
"slack_token" : "tokenSecret"
]
)
```


### Fabrics

Permet de lancer un build pour uploader un IPA vers Fabrics beta.
Paramètre :
* project_name
* project_arch, optionnel permet de spécifier l'archi utilisé par XcodeBuild
* slack_token
* crashlytics_api_key
* crashlytics_secret
* crashlytics_team
* crashlytics_changelog, optionnel log de la version attention cela doit être le chemin vers un fichier et non pas une chaine de caractères

Exemple d'utilisation :
```
// Dans un Jenkinsfile
fabrics(
[
"project_name": "monprojet",
"slack_token" : "tokenSecret",
"crashlytics_api_key": "apikey",
"crashlytics_secret": "secret",
"crashlytics_team": "mateam",
"crashlytics_changelog": "path fichier changelog",
]

```
