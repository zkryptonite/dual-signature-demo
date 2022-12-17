rm *.pem 

#1. Generate CA's private key and self-signed certificate
openssl req -x509 -newkey rsa:4096 -days 365 -nodes -keyout ca-key.pem -out ca-cert.pem -subj "/C=VN/ST=Ha Noi/L=Ha Noi/O=Root/OU=Education/CN=*.root.com/emailAddress=rootca@gmail.com"

echo "CA's self-signed certificate"
openssl x509 -in ca-cert.pem -noout -text 

#2. Generate customer's private key and certificate signing request (CSR)
openssl req -newkey rsa:4096 -days 365 -nodes -keyout customer-key.pem -out customer-req.pem -subj "/C=VN/ST=Ha Noi/L=Ha Noi/O=Customer/OU=Education/CN=*.customer.com/emailAddress=customer@gmail.com"

#3. Use CA's private key to sign customer's CSR and get back the signed certificate
openssl x509 -req -in customer-req.pem -CA ca-cert.pem -CAkey ca-key.pem  -CAcreateserial -out customer-cert.pem

#4. Verify certificate
openssl verify -CAfile ca-cert.pem customer-cert.pem
