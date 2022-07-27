class StudentInfoPage extends TeacherInfoPage {
    static TblName = 'student';
    constructor(tblname) {
        super(tblname);
    }
    display() {
        StudentInfoPage.clean();
        StudentInfoPage.DisplayData();
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
        //getTableData(StudentInfoPage.TblName);
        var tblNameList = ["student", "faculty"];
        var JoinClause = ["student.fno", "faculty.id"];
        queryWithNaturalJoin(tblNameList, JoinClause, '');

        var tabledata = QueryResult.get(StudentInfoPage.TblName);
        if (tabledata == null || tabledata.length == 0) return;
        var append = true;
        if (this.InfoTable != null) {
            this.InfoTable.remove();
            append = false;
        }
        this.InfoTable = generateTable2D(tabledata.length, tabledata[0].length, tabledata, StudentInfoPage.TblName);
        this.InfoTable.id = "InfoTable";
        if (append)
            document.getElementById('mid').appendChild(this.InfoTable);
        else {
            var insertdiv = document.getElementById('InsertDiv');
            document.getElementById('mid').insertBefore(this.InfoTable, insertdiv);//TODO:不能直接用InsertDiv
        }

    }
    static DisplayQueryResult() {
        if (this.QueryTable != null) {
            this.QueryTable.remove();
        }
        var tabledata = QueryResult.get(StudentInfoPage.TblName);
        if (tabledata == null || tabledata.length == 0) return;
        this.QueryTable = generateTable2D(tabledata.length, tabledata[0].length, tabledata, StudentInfoPage.TblName);
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
            var colnames = ColumnInfo.get(StudentInfoPage.TblName);

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
            insertIntoTable(StudentInfoPage.TblName, data_map);
            StudentInfoPage.DisplayData();
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
            var colnames = ColumnInfo.get(StudentInfoPage.TblName);
            var children = div.children;
            var index = 0;
            for (var i in children) {
                if (children[i].localName == 'input') {
                    var value = children[i].value;
                    if (value != '') {
                        var placeholder = children[i].placeholder;
                        var columnname
                        if (TextToColumnName[placeholder] == null)
                            columnname = StudentInfoPage.TblName + "." + colnames[index];
                        else
                            columnname = TextToColumnName[placeholder];
                        //加到whereClause中
                        whereClause += (columnname + '=\'' + value + '\' and ');
                    }
                    index++;
                }//if
            }//for
            whereClause = whereClause.substring(0, whereClause.length - 5);
            //alert(whereClause);
            var tblNameList = ["student", "faculty"];
            var JoinClause = ["student.fno", "faculty.id"];
            queryWithNaturalJoin(tblNameList, JoinClause, whereClause);
            StudentInfoPage.DisplayQueryResult();
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
            var colnames = ColumnInfo.get(StudentInfoPage.TblName);

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
            deleteFromTable(StudentInfoPage.TblName, data_map);
            StudentInfoPage.DisplayData();
        }//function
        button.width = "30px";
        button.innerHTML = "提交";

        div.appendChild(button);
        document.getElementById('mid').appendChild(div);
    }
}

