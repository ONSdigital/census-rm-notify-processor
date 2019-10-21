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
This separation into two queues prevents the service spamming census-rm-uac-qid-service and generating multiple UAC-QID pairs 
if the Gov Notify is unavailable. 



