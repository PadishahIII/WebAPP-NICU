var TableData = new Map()// tblname => data_array
var QueryResult = new Map()//同TableData
var TableThreads = {
    "teacher": ['ID', '姓名', '年龄', '工资', '研究方向', '科研项目', '实验室', '所在院系'],
    "student": ['ID', '姓名', '性别', '年龄', '年级', '所在院系'],
    "course": ['ID', '课程名', '学分', '所在教室'],
    "laboratory": ['ID', '实验室名称'],
    "faculty": ['ID', '院系名称', '资金'],
    "project": ['ID', '项目名称'],
    "direction": ["ID", "研究方向名称"],
    "teach_relation": ["课程名", "教师姓名", "学时"],
    "study_relation": ['课程名', "学生姓名", '成绩']
}
var TextToColumnName = {
    "研究方向": "direction.name",
    "科研项目": "project.name",
    "实验室": "laboratory.name",
    "所在院系": "faculty.name",
    "所在教室": "classroom.building",
    "课程名": "course.name",
    "教师姓名": "teacher.name",
    "学时": "teach_relation.credit_hour",
    "学生姓名": "student.name",
    "成绩": "study_relation.grade"
}
class TeacherInfoPage {
    static TblName = 'teacher';
    tblname;
    InfoTable = null;
    InsertDiv;
    QueryDiv;
    QueryTable;
    DeleteDiv;
    constructor(tblname) {
        this.tblname = tblname;
    }
    display() {
        TeacherInfoPage.clean();
        TeacherInfoPage.DisplayData();
        this.buildInsertDiv();
        this.buildQueryDiv();
        this.buildDeleteDiv();
    }
    static clean() {
        var mid = document.getElementById('mid');
        var nodeCount = 0;
        var children = mid.childNodes;
        for (var i in children) {
            if (children[i].nodeType == 1)
                nodeCount++;
        }
        while (nodeCount > 0) {
            children = mid.childNodes;
            for (var i in children) {
                if (children[i].nodeType == 1) {
                    children[i].remove();
                    nodeCount--;
                }
            }
        }
    }
    static DisplayData() {
        //getTableData(TeacherInfoPage.TblName);
        var tblNameList = ["teacher", "direction", "project", "laboratory", "faculty"];
        var JoinClause = ["teacher.dno", "direction.id", "teacher.pno", "project.id", "teacher.lno", "laboratory.id", "teacher.fno", "faculty.id"];
        queryWithNaturalJoin(tblNameList, JoinClause, '');

        var tabledata = QueryResult.get(TeacherInfoPage.TblName);
        if (tabledata == null || tabledata.length == 0) return;
        var append = true;
        if (this.InfoTable != null) {
            this.InfoTable.remove();
            append = false;
        }
        this.InfoTable = generateTable2D(tabledata.length, tabledata[0].length, tabledata, TeacherInfoPage.TblName);
        this.InfoTable.id = "InfoTable";
        if (append)
            document.getElementById('mid').appendChild(this.InfoTable);
        else {
            var insertdiv = document.getElementById('InsertDiv');
            document.getElementById('mid').insertBefore(this.InfoTable, insertdiv);//TODO:不能直接用InsertDiv
        }

    }
    static DisplayQueryResult() {
        //getTableData(TeacherInfoPage.TblName);
        if (this.QueryTable != null) {
            this.QueryTable.remove();
        }
        var tabledata = QueryResult.get(TeacherInfoPage.TblName);
        if (tabledata == null || tabledata.length == 0) return;
        this.QueryTable = generateTable2D(tabledata.length, tabledata[0].length, tabledata, TeacherInfoPage.TblName);
        document.getElementById('mid').appendChild(this.QueryTable);
    }
    buildInsertDiv() {
        var div = document.createElement('div');
        div.id = "InsertDiv";
        document.getElementById('mid').appendChild(div);
        this.InsertDiv = div;
        var label = document.createElement('label');
        label.innerHTML = "插入数据:";
        div.appendChild(label);

        var tabledata = TableThreads[this.tblname];
        for (var i in tabledata) {
            var input = document.createElement('input');
            input.type = "text";
            input.placeholder = tabledata[i];
            div.appendChild(input);
        }
        var button = document.createElement('button');
        button.onclick = function () {
            var data_map = new Map();
            var colnames = ColumnInfo.get(TeacherInfoPage.TblName);

            var children = div.children;
            var index = 0;
            for (var i in children) {
                if (children[i].localName == 'input') {
                    var value = children[i].value;
                    if (value == '') {
                        alert(children[i].placeholder + "字段不能为空");
                        return;
                    }
                    data_map.set(colnames[index++], value);
                }//if
            }//for
            insertIntoTable(TeacherInfoPage.TblName, data_map);
            TeacherInfoPage.DisplayData();
        }//function
        button.width = "30px";
        button.innerHTML = "提交";

        div.appendChild(button);
    }
    buildQueryDiv() {
        var div = document.createElement('div');
        this.QueryDiv = div;
        div.id = "QueryDiv";
        var label = document.createElement('label');
        label.innerHTML = "查询:";
        div.appendChild(label);

        var tabledata = TableThreads[this.tblname];
        for (var i in tabledata) {
            var input = document.createElement('input');
            input.type = "text";
            input.placeholder = tabledata[i];
            div.appendChild(input);
        }
        var button = document.createElement('button');
        button.onclick = function () {
            var whereClause = new String();
            var colnames = ColumnInfo.get(TeacherInfoPage.TblName);
            var children = div.children;
            var index = 0;
            for (var i in children) {
                if (children[i].localName == 'input') {
                    var value = children[i].value;
                    var placeholder = children[i].placeholder;
                    var columnname
                    if (TextToColumnName[placeholder] == null)
                        columnname = TeacherInfoPage.TblName + "." + colnames[index];
                    else
                        columnname = TextToColumnName[placeholder];
                    if (value != '') {
                        //加到whereClause中
                        whereClause += (columnname + '=\'' + value + '\' and ');
                    }
                    index++;
                }//if
            }//for
            whereClause = whereClause.substring(0, whereClause.length - 5);
            //alert(whereClause);
            var tblNameList = ["teacher", "direction", "project", "laboratory", "faculty"];
            var JoinClause = ["teacher.dno", "direction.id", "teacher.pno", "project.id", "teacher.lno", "laboratory.id", "teacher.fno", "faculty.id"];
            queryWithNaturalJoin(tblNameList, JoinClause, whereClause);
            //queryTableData(TeacherInfoPage.TblName, whereClause);
            TeacherInfoPage.DisplayQueryResult();
        }//function
        button.width = "30px";
        button.innerHTML = "提交";

        div.appendChild(button);
        document.getElementById('mid').appendChild(div);
    }
    buildDeleteDiv() {
        var div = document.createElement('div');
        this.DeleteDiv = div;
        div.id = "DeleteDiv";
        var label = document.createElement('label');
        label.innerHTML = "移除:";
        div.appendChild(label);

        var input = document.createElement('input');
        input.type = "text";
        input.placeholder = "ID";
        div.appendChild(input);

        var button = document.createElement('button');
        button.onclick = function () {
            var whereClause = new String();
            var colnames = ColumnInfo.get(TeacherInfoPage.TblName);

            var data_map = new Map();

            var children = div.children;
            var index = 0;
            for (var i in children) {
                if (children[i].localName == 'input') {
                    var value = children[i].value;
                    if (value != '') {
                        data_map.set("id", value);
                    }
                    index++;
                }//if
            }//for
            deleteFromTable(TeacherInfoPage.TblName, data_map);
            TeacherInfoPage.DisplayData();
        }//function
        button.width = "30px";
        button.innerHTML = "提交";

        div.appendChild(button);
        document.getElementById('mid').appendChild(div);
    }
}

//清除元素id下的所有节点
function clean(id) {
    var mid = document.getElementById(id);
    var nodeCount = 0;
    var children = mid.childNodes;
    for (var i in children) {
        if (children[i].nodeType == 1)
            nodeCount++;
    }
    while (nodeCount > 0) {
        children = mid.childNodes;
        for (var i in children) {
            if (children[i].nodeType == 1) {
                children[i].remove();
                nodeCount--;
            }
        }
    }
}

//获取所有表格的名字及字段名
/**
 * 返回内容：
 * {
 *  "num":数量
 *  "tblnames":"tbl1;tbl2;..."
 *  "colnames":"col1_1,col1_2..;col2_1,col2_2..."
 * }
 */
var TableNames = new Array()// {'num' => 12 str1 str2 }
var ColumnInfo = new Map()//tblname => Array(col1,col2)
function getTables() {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("GET", "/DBManager/getTables", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send()
    var data = xmlhttp.responseText
    //alert(data)
    console.log(data)
    var json = JSON.parse(data)
    TableNames['num'] = json['num']
    var tblnames = json['tblnames'].split(';')
    var cols = json['colnames'].split(';')
    for (var i in tblnames) {
        if (tblnames[i] != '') {
            TableNames.push(tblnames[i])
        }
    }
    for (var i in cols) {
        if (cols[i] != '') {
            var tblname = TableNames[i]
            var col = new Array()
            var cols_i = cols[i].split(',')
            for (var j in cols_i) {
                if (cols_i[j] != '') {
                    col.push(cols_i[j])
                }
            }//for j
            ColumnInfo.set(tblname, col)
        }//if
    }//for i
}

/**
 * 查询指定表的所有元组
 * 返回结果：
 * {
 *  "num":9   //元组个数
 *  "0":"aa;bb;cc;"
 *  "1":"dd;ee;ff"
 *  ...
 * }
 */
function getTableData(tblname) {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("GET", "/DBManager/queryAllData?tblname=" + tblname, false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send()
    var data = xmlhttp.responseText

    //alert(data)
    var json = JSON.parse(data)

    var num = json['num']
    var col_datas = new Array()
    var i = 0
    while (i < num) {
        var col_data_str = json[String(i)]
        var col_data_strs = col_data_str.split(";")
        var col_data_arr = new Array()
        for (var j in col_data_strs) {
            if (col_data_strs[j] != '') {
                col_data_arr.push(col_data_strs[j])
            }//if
        }//for j
        col_datas.push(col_data_arr)
        i++
    }//while i
    TableData.set(tblname, col_datas)
}
function queryTableData(tblname, whereClause) {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/queryTableData", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&whereClause=" + whereClause)
    var data = xmlhttp.responseText

    if (data[0] != '{') {
        alert(data)
    }
    var json = JSON.parse(data)

    var num = json['num']
    var col_datas = new Array()
    var i = 0
    while (i < num) {
        var col_data_str = json[String(i)]
        var col_data_strs = col_data_str.split(";")
        var col_data_arr = new Array()
        for (var j in col_data_strs) {
            if (col_data_strs[j] != '') {
                col_data_arr.push(col_data_strs[j])
            }//if
        }//for j
        col_datas.push(col_data_arr)
        i++
    }//while i
    QueryResult.set(tblname.split(',')[0], col_datas)
}
/**
 * 自然连接查询
 * @param {Array} tblNameList 查询需要的表 tb1 tb2
 * @param {Array} JoinClause 自然连接的条件  tbl1.a = tb2.b  
 * @param {*} whereClause 查询条件
 * 发送格式：
 * tblnames:用逗号隔开的表名
 * whereClause:完整的where子句
 */
function queryWithNaturalJoin(tblNameList, JoinClause, whereClause) {
    var tblnames = new String()
    for (var i in tblNameList) {
        tblnames += tblNameList[i]
        tblnames += ','
    }
    tblnames = tblnames.substring(0, tblnames.length - 1)
    //alert(tblnames)

    var joincondition = new String();
    var len = JoinClause.length
    var index = 0
    while (index + 1 < len) {
        joincondition += JoinClause[index]
        joincondition += '='
        joincondition += JoinClause[index + 1]
        joincondition += ' and '
        index += 2
    }
    if (whereClause == '')
        joincondition = joincondition.substring(0, joincondition.length - 5)
    var whereCondition = joincondition + whereClause
    //alert(whereCondition)

    queryTableData(tblnames, whereCondition)

}

/**
 * 向指定表插入一个元组
 * data_map:<列名，属性值>
 * @param {*} tblname 
 * @param {*} data_map 
 */
function insertIntoTable(tblname, data_map) {
    if (data_map.size != ColumnInfo.get(tblname).length) {
        alert("Error1 at insertIntoTable!")
        return false
    }
    var postString = new String()
    for (var i of data_map.keys()) {
        postString += i
        postString += '=' + data_map.get(i)
        postString += "&"
    }
    postString = postString.slice(0, postString.length - 1)
    console.log("poststr:")
    console.log("tblname=" + tblname + "&" + postString)
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/insert", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&" + postString)

    var data = xmlhttp.responseText
    alert(data)

}

/**
 * 删除指定表的一条数据
 * @param {*} tblname 
 * @param {*} data_map 
 */
function deleteFromTable(tblname, data_map) {
    var postString = new String()
    for (var i of data_map.keys()) {
        postString += i
        postString += '=' + data_map.get(i)
        postString += "&"
    }
    postString = postString.slice(0, postString.length - 1)
    console.log("poststr:")
    console.log("tblname=" + tblname + "&" + postString)
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/delete", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&" + postString)

    var data = xmlhttp.responseText
    alert(data)
}

/**
 * 更新一条数据
 * 发送格式：
 * tblname=xx
 * data_map_json:
 * {
 *  "old":{
 *         "col1":value1,
 *         "col2":value2...
 *        }
 *  "new":{同old}
 * }
 * @param {*} tblname 
 * @param {Map} old_data_map 旧值
 * @param {Map} new_data_map 新值
 * @returns 
 */
function updateFromTable(tblname, old_data_map, new_data_map) {
    var old_jsonstr = JSON.stringify(Map2Obj(old_data_map))
    var new_jsonstr = JSON.stringify(Map2Obj(new_data_map))
    var data_map_json = {
        "old": old_jsonstr,
        "new": new_jsonstr
    }
    var data_map_jsonstr = JSON.stringify(data_map_json)

    console.log(data_map_jsonstr)

    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/update", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&data_map_json=" + data_map_jsonstr)

    var data = xmlhttp.responseText
    alert(data)
}
function Map2Obj(map) {
    let obj = Object.create(null)
    for (let [k, v] of map) {
        obj[k] = v
    }
    return obj
}

/**
 * 生成指定大小的表格
 * @param {*} row 
 * @param {*} col 
 * @param {*} data_arr row大小的数组，每个元素为col大小的数组
 */
function generateTable2D(row, col, data_arr, thread) {
    table = document.createElement("table")
    tBody = document.createElement("tBody")
    if (thread != '') {
        AddThreadToTable(thread, tBody)
    }
    for (var i = 0; i < row; i++) {
        //tr = table.insertRow(i)
        var tr = document.createElement('tr')
        var arr = data_arr[i]
        for (var j = 0; j < col; j++) {
            //td = tr.insertCell(j)
            var td = document.createElement('td')
            td.innerHTML = arr[j]
            tr.appendChild(td)
        }
        tBody.appendChild(tr)
    }
    table.appendChild(tBody)
    document.body.appendChild(table)
    return table;
}
/**
 * 生成一维纵向或横向表
 * @param {*} num 
 * @param {*} data_arr 
 * @param {*} type 0 for 纵向  1 for 横向
 */
function generateTable1D(num, data_arr, type, thread) {
    table = document.createElement("table")
    tBody = document.createElement("tBody")
    if (thread != '') {
        AddThreadToTable(thread, tBody)
    }
    if (type == 0) {
        for (var i = 0; i < num; i++) {
            tr = tBody.insertRow(i)
            td = tr.insertCell(0)
            td.innerHTML = data_arr[i]
        }
    }
    else if (type == 1) {
        tr = tBody.insertRow(0)
        for (var i = 0; i < num; i++) {
            td = tr.insertCell(i)
            td.innerHTML = data_arr[i]
        }
    }
    else console.log("Error in generateTable1D")
    table.appendChild(tBody)
    document.body.appendChild(table)
    return table;
}
function AddThreadToTable(tblname, tableobj) {
    var tharr = TableThreads[tblname]
    var tr = document.createElement('tr')
    for (var i in tharr) {
        var th = document.createElement('th')
        th.innerHTML = tharr[i]
        tr.appendChild(th)
    }
    tableobj.appendChild(tr)
}
//console.log(TableThreads['teacher'])
//var data_map = new Map()
//data_map.set("id", 1)
//data_map.set("name", "aa")
//data_map.set("funding", "200")
//var str = JSON.stringify(Map2Obj(data_map))
//console.log(str)
//for (var i of data_map) {
//    console.log(i)
//    console.log(data_map.get(i))
//}
//var s = new String("abc")
//console.log(s.length)
//console.log(s.substring(0, s.length - 1))
//console.log(s.slice(0, s.length - 1))
//var arr = new Array()
//arr.push(1)
//console.log(arr.length)
//
//var map = new Map()
//map.set("a", 1)
//map.set("b", 2)
//console.log(map.keys())
//for (var i of map.keys()) {
//    console.log(i)
//    console.log(map.get(i))
//}
//console.log(map.size)
/**
 * 获取
 */
//
//class a {
//    n;
//    static s;
//    constructor(nn) {
//        this.n = nn;
//        console.log("a");
//    }
//    func() {
//        console.log("func");
//    }
//}
//class b extends a {
//    constructor(nn) {
//        //super(nn);
//        console.log("b");
//    }
//    func() {
//        console.log("bfunc");
//    }
//}
//var bb = new b("b");
//console.log(bb.n);
//bb.func();

//var s = 'a'
//console.log(s.split(',')[0])
//console.log(TextToColumnName['a'] == null)