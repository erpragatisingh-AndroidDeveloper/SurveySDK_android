#Welcome to the Survey.com android SDK and demo application

####Clone this repo to get started working with the Survey.com SDK

Please contact your account rep to get your ["ORGANIZATION_ID"](https://github.com/Survey-Com/survey_sdk_publicdemo/wiki/Getting-an-org_id) before going live, the sample "ORGANIZATION_ID" is for test data only.

##Quick Start
Import android projects SurveySDK, SurveyDemo and google-play-services_lib to your android IDE. These projects were created using Eclipse, but you can use Android Studio with these projects easily. SurveySDK and google-play-services_lib are library projects and when imported ensure that Is Library option is checked in Properties -> Android settings for these projects. 

google-play-services_lib library project should be imported first. If you already use google-play-services in your development environment, then you do not need to import this library project again. When importing SurveySDK project, check that the reference to google-play-services in Properties -> Android settings is correct. If not, remove and add the reference again from the correct location in your environment. Also ensure that Is Library option is checked for SurveySDK project.

Finally, import SurveyDemo project and update Properties -> Android settings to reference google-play-services_lib and SurveySDK library projects correctly. Now compile demo app and run it with android emulator or deploy to your android device. 

_All usage must adhere to our [SDK Usage Agreement and Guideslines](https://github.com/Survey-Com/survey_sdk_publicdemo/wiki/SDK-Usage-Agreement)_
