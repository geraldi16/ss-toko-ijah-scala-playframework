# ss-toko-ijah-scala-playframework
Used:
- scala play framework 2.6
- anorm
- spoiwo
Database : SQLite

Simple Script :
=======================
<not defined yet>

Toko Ijah Inventory Activities:
==========================
1. Store jumlah barang
routes : `/create/jumlah-barang`
method : POST
input example: ```{
                  	"sku":"abcd",
                  	"item_name":"baju monyet baru",
                  	"jumlah":105
                  }```

2. Store barang masuk
routes : `/create/barang-masuk`
method : POST
input example:```{
                 	"sku":"abc",
                 	"item_name":"baju",
                 	"waktu": "2018-01-01 01:00:00",
                 	"pemesanan":100,
                 	"terima":99,
                 	"beli":14000,
                 	"total":12345678,
                 	"kwitansi":"ID-0001",
                 	"catatan": "bagus kok barangnya, ga da masalah"
                 }```


3. Store barang keluar
routes : `/create/barang-keluar`
method : POST
input example : ```{
                   	"sku":"abc",
                   	"item_name":"baju",
                   	"waktu": "2018-01-01 01:00:00",
                   	"keluar":99,
                   	"jual":14000,
                   	"total":12345678,
                   	"catatan": "terjual"
                   }```

4. show laporan penjualan barang
routes : `/laporan/penjualan?ds=<yyyy-mm-dd>&de=<yyyy-mm-dd>`
method : GET

5. show laporan nilai barang
routes : `/laporan/nilai-barang`
method : GET