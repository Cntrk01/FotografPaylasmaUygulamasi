package com.example.fotografpaylasmauygulamasi

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_fotograf_paylasma_activty.*
import java.util.*
import java.util.jar.Manifest

class FotografPaylasmaActivty : AppCompatActivity()
{
    var secilenGorsel : Uri?=null
    var secilenBitmap : Bitmap?=null
    private lateinit var storage : FirebaseStorage //5)Kullanacagımıziçin cagırdık
    private lateinit var auth : FirebaseAuth//5)Kullanacagımıziçin cagırdık
    private lateinit var database : FirebaseFirestore//5)Kullanacagımıziçin cagırdık
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fotograf_paylasma_activty)
        storage= FirebaseStorage.getInstance() //5.1)Tanımlaması yapıldı
        auth= FirebaseAuth.getInstance()//5.1)Tanımlaması yapıldı
        database= FirebaseFirestore.getInstance()//5.1)Tanımlaması yapıldı
    }

    fun gorselTiklaVeSec(view:View)
    {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED)
        {//1)izin verilmedi izin alacagiz.
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{//2)zaten izin vermiş işlemleri yapcaz
            val galeri=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeri,2)
        }
    }
    override fun onRequestPermissionsResult(//3)İstediğimiz izinlerin sonucunda ne olacak.Onu yapcaz.İzin verilirse galeri açılacak.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1)//3.0)İstediğimiz koda bakıyoruz
         {
            if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) //3.1)Elde bir sonuc varmı && bana izin verildimi
            {
                val galeri=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //3.2Varsa zaten galeriyi aciyoruz
                startActivityForResult(galeri,2)
            }

         }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//4.Adım İzin verilmişse burası yapılacak.
        if(requestCode==2 && resultCode==Activity.RESULT_OK && data !=null)
        //4.1)kod2 ve resultCode okey denmişse.Ama kullanıcı vazgeçebilir ondan Activty.RESULT_OK yapıyoruz ve data 0dan farklıysa
        {//4.2)onCreate üzerinde var secilenGorsel : Uri?=null var secilenBitmap : Bitmap?=null tanımlandı .  secilenGorseli Bitmapa cevircez
            secilenGorsel=data.data //4.3)Urimiz var
            if (secilenGorsel !=null) //4.4)Eger secilen görsel varsa
            {//4.5)Bitmapi alcaz
                if(Build.VERSION.SDK_INT>28)
                {
                    val source=ImageDecoder.createSource(this.contentResolver,secilenGorsel!!) //4.8)Versiyonu 28 den büyük cihazlar için erişim
                    secilenBitmap=ImageDecoder.decodeBitmap(source)
                    gorselSec.setImageBitmap(secilenBitmap)
                }else{
                    secilenBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,secilenGorsel) //4.6)!!!!bunlar eski methotlar eski sdk'e sahip telefonlarda çalışması içindir !!
                    gorselSec.setImageBitmap(secilenBitmap) //4.7)gorsele bitmapi verdik
                }

            }


        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun paylas(view : View)
    {
        //5.5)UUID(universal uniq id) ile yüklenen resim üstüne yazılıyor bunun önüne geçicez
        val uuid= UUID.randomUUID() //5.6)Rastgele bizim için ID oluşturuyor.
        val gorselIsim="${uuid}.jpg" //5.7)Görsel isimini şimdi  gorselReference veriyoruz
        val reference=storage.reference //5.2)Biz bu referans sayesinde görseli nereye kayıt edecegimizi belirtebiliyoruz.reference ile firebaseye istediğimiz yere erişebiliyoruz
        val gorselReference=reference.child("images").child(gorselIsim) //5.3)1.child firebasedeki storede bulunan images klasörü.2.si ise seçilen görseli bu klasör altında kayıt edecek oraya referans verdik
        if(secilenGorsel !=null) //5.4)Seçilen görseli yükleyecegiz bosmu kontrol ediyoruz.Degilse if calisiyor
         {//Burada yüklenen görselin boyutu büyük oldugu için biraz süreye ihtiyac var asenkron olayı uzuyor.
            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {task->
                val yuklenenGorselReferansı=FirebaseStorage.getInstance().reference.child("images").child(gorselIsim) //5.8)gorselReference'nin aynısı ama bu yüklendikten sonra oluşturulmuş oldu !!
                yuklenenGorselReferansı.downloadUrl.addOnSuccessListener { uri-> //5.8.1)Yukarıdaki işlemi yaptıgımız için dowloadUrl çıktı uri=url
                    val downloadUrl=uri.toString() //!!!!!!!!!!!urlyi yakalamış olduk.Bunu veritabanına kayıt edicez !!!!!!!!!
                    val guncelKullanici=auth.currentUser!!.email.toString() //!!!!!!!!!!!!!!!!!!Kullanici adini da burda aldık!!!!!!!!!!!!!!!!!!!
                    val tarih=Timestamp.now() //Timestamp methodu firebase olanı seçtik
                    val postHasMap = hashMapOf<String,Any>() //5.9)Firebase Database kısımının mantıgı hasmap ile çalışır.Verileri HashMapa ekleyip databaseye göndercez!!!!!!!!!
                    postHasMap.put("GorselinUrlsi",downloadUrl) //""içindeki rasgtle isim verdik
                    postHasMap.put("KullaniciAdi",guncelKullanici)
                    postHasMap.put(("Tarih"),tarih)
                    if(yorumGirilenSatir.text.toString()==null)
                    {
                        Toast.makeText(applicationContext,"Lütfen Yorum Girerek Paylaşım Yapınız",Toast.LENGTH_LONG).show()
                    }
                    else
                    {
                        val kullaniciYorumu=yorumGirilenSatir.text.toString()
                        postHasMap.put("Yorum",kullaniciYorumu)
                    }
                    database.collection("Post").add(postHasMap).addOnCompleteListener { task-> //5.10)collection hangi koleksyondan işlem yapacagımızı belirtoyuz
                        //add ile ekleme işlemini yapıyoruz. Complete ile tamamlandıgı zaman asagıyı yapcaz
                        if(task.isSuccessful)
                        {
                            finish() //!!Biz burda HaberlerActivtyden geliyoruz.Orda intentten sonra kapatma işlemi yapmadık burda yapınca oraya dönecek
                        }
                    }.addOnFailureListener { exception-> //Başarılı olmazsa hat abascak
                        Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener { exception-> //Yükleme yaparkende hata gerçekleşebilir ondan ekledik
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }
}