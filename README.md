# census-rm-notify-processor
This service is a handler for UAC fulfilment events to send messages to GOV.notify that deliver a new UAC via SMS text message.
The SMS messages are sent using the [GOV.UK Notify](https://www.notifications.service.gov.uk/) Service.


# Queues
This service connects to 
1. notify.fulfilments
2. notify.enriched.fulfilments

# Fulfilment codes

The following fulfilment codes are currently supported:

| Detail | Code |
|-----------------------------------------|-------:|
|Household UAC England | UACHHT1|
|Household UAC Wales (English) | UACHHT2|
|Household UAC Wales (Welsh) | UACHHT2W|
|Household UAC Northern Ireland | UACHHT4|
|Individual UAC England | UACIT1|
|Individual UAC Wales (English) | UACIT2|
|Individual UAC Wales (Welsh) | UACIT2W|
|Individual UAC Northern Ireland | UACIT4|
|CE UAC England | UACCET1|
|CE UAC Wales (English) | UACCET2|
|CE UAC Wales (Welsh) | UACCET2W|
|Individual access code requested via eQ for England | UACITA1|
|Individual access code requested via eQ for Wales (Bilingual) | UACITA2B|
|Individual access code requested via eQ for Northern Ireland | UACITA4|

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
  UACHHT1: ce1e545e-f50f-455b-a394-88b49a36fa0c
  UACHHT2W: 67dc02ff-a667-4b21-8e4e-61dd28936b8c
  UACHHT2: 2375c493-359d-4712-938e-bf97a641f18f
  UACHHT4: 5d985237-b446-492a-bd0a-f7368265282c
  UACIT1: b7ec0cde-b55b-460a-a78a-cb80767acdca
  UACIT2W: b2db05f0-d7d1-4b88-b3de-658495bd8a47
  UACIT2: f6de09f2-4d4c-4f62-82e3-b1a24e0cf910
  UACIT4: d490af7d-300a-4eb8-a961-cb4de54332ee
  UACCET1: 21f22f8d-2642-444e-9d13-a54b87647a93
  UACCET2W: 2c12a125-4035-4b81-9988-204e02e759a5
  UACCET2: b2b9e650-cb22-49d7-b5da-95169e13ea12
  UACITA1: c6548c71-abd0-4990-aeb5-a7d5854f8da0
  UACITA2B: 203b931c-4a79-48f4-8475-6dd8acf04d9b
  UACITA4: ef3c0cfe-582e-4952-9316-d8f602a8323a
  senderId: ae14c5b3-f317-4051-834c-644f0c236347

caseapi:
  host: localhost
  port: 8161
```

Open the census-rm-notify-processor repository in IntelliJ.
Create a SpringBoot Run configuration called Application.
Run in debug mode.


# Testing

Use the [census-rm-acceptance-tests](https://github.com/ONSdigital/census-rm-acceptance-tests)