# census-rm-notify-processor
This service is a handler for UAC fulfilment events to send messages to GOV.notify that deliver a new UAC via SMS text message.
The SMS messages are sent using the [GOV.UK Notify](https://www.notifications.service.gov.uk/) Service.


# Queues
This service connects to 
1. notify.fulfilments
2. notify.enriched.fulfilments

# Fulfilment codes

The following fulfiment codes are currently supported.

| Detail | Code |
|-----------------------------------------|-------:|
|Individual questionnaire request England | UACIT1|
|Individual questionnaire request Wales (English) | UACIT2|
|Individual questionnaire request Wales (Welsh) | UACIT2W|
|Individual questionnaire request Northern Ireland | UACIT4|

# Overview

Fulfilment requests come via the Events exchange to the notify.fulfilments queue.

When a message is received the [census-rm-uac-qid-service](https://github.com/ONSdigital/census-rm-uac-qid-service) is used to generate a new UAC QID pair.

The service then queues an enriched request message on the notify.enriched.fulfiments queue. Messages from this queue are processed and Gov Notify is used to send the SMS.
This separation into two queues prevents the service spamming census-rm-uac-qid-service and generating multiple UAC-QID pairs if the Gov Notify is unavailable. 


# Configuration

By default the src/main/resources/application.yml is configured for [census-rm-docker-dev](https://github.com/ONSdigital/census-rm-docker-dev)

For production the configuration is overridden by the K8S apply script.

## To debug census-rm-notify-processor locally 

Start docker-dev

Stop census-rm-notify-processor running in Docker

```yaml
docker stop notify-processor
```

To run the service locally for debugging override the application.yml configuration with

```yaml
notify:
  apiKey: dummykey-ffffffff-ffff-ffff-ffff-ffffffffffff-ffffffff-ffff-ffff-ffff-ffffffffffff
  baseUrl: http://localhost:8917
  templateEnglish: 21447bc2-e7c7-41ba-8c5e-7a5893068525
  templateWelsh: ef045f43-ffa8-4047-b8e2-65bfbce0f026
  templateWelshAndEnglish: 23f96daf-9674-4087-acfc-ffe98a52cf16
  templateNorthernIreland: 1ccd02a4-9b90-4234-ab7a-9215cb498f14
  senderId: b7e75a6c-8f7f-4264-8b34-38bb5831270e

caseapi:
  host: localhost
  port: 8161
```

Open the census-rm-notify-processor repository in IntelliJ.
Create a SpringBoot Run configuration called Application.
Run in debug mode.


# Testing

Use the [census-rm-acceptance-tests](https://github.com/ONSdigital/census-rm-acceptance-tests)