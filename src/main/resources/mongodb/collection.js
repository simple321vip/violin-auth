# use violin
db.createCollection("t_tenant")

// authority    0 システム管理者
//           　 1 一般ユーザー
// 　　         2

{
 "_id": "xxxxxxx",
 "id": "",
 "account": "",
 "tel": 13333333333,
 "authority": 0
}



db.createCollection("t_bookmark")

{
 "_id": "xxxxxxx",
 "id": "xxxxxx",
 "comment": "xxxxxxx",
 "isDelete": false,
 "typeId": 1111,
 "url": "xxxxxxxx"
}