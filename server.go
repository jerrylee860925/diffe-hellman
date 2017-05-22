package main

import (
    "fmt"
    "net"
    "io"
    "bytes"
    "math/big"
    "math/rand"
	"errors"
	"crypto/des"
	"strings"

)
var publickey *big.Int ; 

func ClientListen(port string) {

    ln, err := net.Listen("tcp", port)
    if err != nil {
        fmt.Println("error\n")
        fmt.Println(err)
      return
    }
    for {
        nc, err := ln.Accept()
        if err != nil {
            fmt.Println(err)
            continue
        }
      go recivemsg(nc)
    }
}
func recivemsg(nc net.Conn){
	publickey= big.NewInt(15485863);
	gen:= big.NewInt(48);
	rawkey := big.NewInt(0);
	myraw1:= big.NewInt(0);
    var msg []byte
    var buf bytes.Buffer
    io.Copy(&buf, nc)
    //var tcpnc *TCPConn;
    fmt.Println("total size:", buf.Len())
    msg = buf.Bytes()
    rawkey=rawkey.SetBytes(msg);
    fmt.Println("byte array is ", msg);
    fmt.Println("recived from client ", rawkey);
    r := rand.New(rand.NewSource(99));
	myraw := big.NewInt(0);
	myraw = myraw.Rand(r,publickey);
	fmt.Println(myraw," my key before modulo");
	myraw1.Exp(gen,myraw,publickey);
	fmt.Println("after modulo the key is ", myraw1)
	key := rawkey.Exp(rawkey,myraw,publickey)
    fmt.Println(" final key is ",key);
    fmt.Println(key)
 	newmsg:=myraw1.Bytes();
 	fmt.Println(newmsg)
 	nc.Write(newmsg)
 
 	 
 	/*des starts here */
 	tempary := key.Bytes();
 	fmt.Println(len(tempary));
 	var shift uint 

 	if(len(tempary)<8){
 		count := 8-len(tempary)
 		shift = 8*uint(count);
 		fmt.Println("key needs to be shifted by ",shift," bits")
 		key = key.Lsh(key,shift)
 	}
 	tempary = key.Bytes();
 	fmt.Println(len(tempary),"    ",key);
 	fmt.Println(tempary)
 	clientIP :=nc.RemoteAddr()
 	fmt.Println(clientIP.String())
 	ip := strings.Split(clientIP.String(),":")[0] 
 	fmt.Println(ip)
 	nc.Close()
 	/***********************************************     des starts here      ************************************************************************/

 	conn, err := net.Dial("tcp", ip+":9998")
 	if err != nil {
			fmt.Println(err)
	}
	 tmp := make([]byte, 256) 
	 bufferread := make([]byte, 0, 4096)
	for {
        n, err := conn.Read(tmp)
        if err != nil {
            if err != io.EOF {
                fmt.Println("read error:", err)
            }
            break
        }
        //fmt.Println("got", n, "bytes.")
        bufferread = append(bufferread, tmp[:n]...)

    }
    fmt.Println("total size:", len(bufferread))
    fmt.Println(bufferread)
    dcm,errorrr := DesDecrypt(bufferread,tempary)
    if errorrr!=nil {
    	fmt.Println(errorrr)
    	
    }
    fmt.Println(dcm)
    finaldcm := unpadding(dcm)
    fmt.Println(finaldcm)

    s := string(finaldcm)
    fmt.Println(s)
    fmt.Println(len(dcm))
 	fmt.Println("/****************************************************************************************************************************/")
    
}

func unpadding(input []byte)[]byte {
	lengthOfInput := int(len(input)) 
	//var newArry []byte;
	lastNumint := int(input[lengthOfInput-1])
	lastNum :=input[lengthOfInput-1]
	fmt.Println(lastNumint," #########")
	var indicator int 
	indicator = 0;
	if(lastNum>0 && lastNum<8){
		for i := lengthOfInput- lastNumint; i < lengthOfInput; i++ {
			if(input[i] != lastNum){
				indicator = 1;
				return input
				break;
			}
		}	
	}
	if indicator==0 {
		newArry := make([]byte,lengthOfInput - lastNumint );
		copy(newArry,input[0:lengthOfInput - lastNumint])
		return newArry
	}
	return input
}



func ZeroUnPadding(origData []byte) []byte {
	return bytes.TrimFunc(origData,
		func(r rune) bool {
			return r == rune(0)
		})
}
func DesDecrypt(src, key []byte) ([]byte, error) {
	block, err := des.NewCipher(key)
	if err != nil {
		return nil, err
	}
	out := make([]byte, len(src))
	dst := out
	bs := block.BlockSize()
	if len(src)%bs != 0 {
		return nil, errors.New("crypto/cipher: input not full blocks")
	}
	for len(src) > 0 {
		block.Decrypt(dst, src[:bs])
		src = src[bs:]
		dst = dst[bs:]
	}
	out = ZeroUnPadding(out)
	// out = PKCS5UnPadding(out)
	return out, nil
}
func main() {
		ClientListen(":9999")
}




