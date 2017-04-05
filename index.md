## Avatar Java client

You can use this java client to send transactions to Avatar plataform.

### Usage

```
com.systemonenoc.avatar.client.JavaClient -help
```

```
usage: Avatar java client [options]
 -bid,--bid <arg>           Buyer TIN
 -c,--cert <arg>            ATAX public certificate
 -ccid,--cid <arg>          Buyer Cost Center Id
 -cl,--client <arg>         Client type. g5 (default) or vsdc
 -e,--endpoint <arg>        Endpoint Url
 -h,--help                  Help command
 -it,--iType <arg>          Invoice type. Normal|Pro Forma|Training
 -m,--mrc <arg>             Machine registration code
 -p,--private <arg>         RSA private key
 -p12,--p12 <arg>           Client certificate for mutual auth.
 -pass,--password <arg>     p12 password
 -phone,--phone <arg>       Buyer phone
 -pl,--place <arg>          Merchant location
 -t,--tin <arg>             TIN number
 -tA,--taxA <arg>           taxA. Default value is 0
 -tB,--taxB <arg>           taxB. Default value is 18
 -tC,--taxC <arg>           taxC. Default value is 16
 -tD,--taxD <arg>           taxD. Default value is 32
 -tr,--transactions <arg>   Number of transactions to send. One single
                            thread.
 -tt,--tType <arg>          Transaction type. Sale|Refund
```
### Samples

```
# VSDC sample
java -cp '/PATH/:/PATH/*' com.systemonenoc.avatar.client.JavaClient \
 -t TIN -m MRC -e HOST -p PRIV.pem -c CERT.pem -cl vsdc -p12 SERIAL_NUMBER.p12 -pass PASS

# API sample
java -cp '/PATH/:/PATH/*' com.systemonenoc.avatar.client.JavaClient \
 -t TIN -m MRC -e HOST -p PRIV.pem -c CERT.pem
```

