package com.example.fotografpaylasmauygulamasi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class KullaniciActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth //ekleme
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth= FirebaseAuth.getInstance() //ekleme yaptık


        val guncelKullanici=auth.currentUser//2.5)Kullanıcı artık giriş yaptı birdaha girişle uğraşmayacak.Hatırlama işlemi ypaıldı.onCreate içinde
        if (guncelKullanici !=null)
        {
            val intent=Intent(this,HaberlerActivity::class.java) //intenti veriyoruz
            startActivity(intent)
            finish()
        }
    }
    fun girisYapTiklaFonksiyon(view : View)
    {
        //2)Asenkron Fonksiyonları kullanarak giriş işlemlerini test ediyoruz.
        auth.signInWithEmailAndPassword(emailAdresi.text.toString(),sifreKısımı.text.toString()).addOnCompleteListener { task-> //2.1)sign methodu ile email,sifre alıyoruz
           if(task.isSuccessful)
           {
               val guncelKullanici=auth.currentUser?.email.toString() //2.2)Kullanıcı giriş yaptımı yapmadımı ondan dolayı currentUser? olmalıdır.
               Toast.makeText(this,"Hoşgeldin : ${guncelKullanici}",Toast.LENGTH_LONG).show()
               val intent=Intent(this,HaberlerActivity::class.java) //2.3)intenti veriyoruz
               startActivity(intent)
               finish() //2.4)yaşamdöngüsü var zaten ondan bitiriyoruz.
           }
        }.addOnFailureListener { exception ->
            Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }
    fun kayıtOlTiklaFonksiyon(view : View) //1)Kayıtolma fonksiyonuna basladık
    {
        val email=emailAdresi.text.toString()
        val sifre=sifreKısımı.text.toString()
        auth.createUserWithEmailAndPassword(email,sifre).addOnCompleteListener { task-> //1.1)adOnComplete {} olanı alıyoruz.İşlem tamamlandıgında diyoruz.Burda
            //1.2)Fail-Commplet-Succes-Cancel methotları da var asagida Fail kullanıcaz if olmazsa
            //asenkron
            if(task.isSuccessful)
            {//1.3)başarılı oldu diger classa gitcez
                val intent= Intent(this,HaberlerActivity::class.java)
                startActivity(intent)
                finish() //aktivite yaşam döngüsü var unutma . bunu sonlandırdım.
            }
        }.addOnFailureListener { Exception-> //1.4)Eğer hata varsa bu blok çalışacak.Toast mesajı vericek
            Toast.makeText(applicationContext,Exception.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }
}