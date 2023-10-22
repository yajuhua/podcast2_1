new Vue({
    el: "#app",
    data() {
        return {
            activeMenu: 'info',
            formInline: {
                user: '',
                password: ''
            },
            logs: [], // 存储日志信息的数组
            systemAllData: [],
            systemAllData1: "",
            infoData: [
                { key: "已运行", value: "" },
                { key: "版本", value: "" },
                { key: "更新时间", value: "" },
                { key: "代号", value: "" }
            ],
            fileList: [],
            multipleSelection: [],
            preparingForUpdates:false
        };
    }, mounted: function () {

        // 页面加载完成后，发送异步请求，查询数据
        var _this = this;
        axios({
            method: "get",
            url: "./system/systemInfoServlet"
        }).then(function (resp) {
            //系统信息
            _this.systemAllData1 = resp.data;
            _this.infoData[0].value = resp.data.systemRuntime;
            _this.infoData[1].value = resp.data.systemVersion;
            _this.infoData[2].value = resp.data.systemUpdate;
            _this.infoData[3].value = resp.data.systemCode;

        })
    },
    created() {
        // 建立 WebSocket 连接
        const socket = new WebSocket("ws://"+ window.location.host +"/podcast2/websocket/logs");

        // 监听 WebSocket 连接事件
        socket.onopen = () => {
            console.log("WebSocket 连接成功");
        };

        // 监听 WebSocket 接收消息事件
        socket.onmessage = (event) => {
            const message = event.data;
            this.logs.push(message); // 将接收到的日志信息添加到数组中
            this.scrollToBottom(); // 滚动到底部
        };

        // 监听 WebSocket 关闭事件
        socket.onclose = () => {
            console.log("WebSocket 连接关闭");
        };
    },
    methods: {
        handleMenuSelect(index) {
            this.activeMenu = index;
        },
        clearLogs() {
            this.logs = []; // 清空日志数组
        },
        scrollToBottom() {
            this.$nextTick(() => {
                const logsContainer = this.$el.querySelector(".logs");
                logsContainer.scrollTop = logsContainer.scrollHeight;
            });
        },
        toSubscriptionList() {
            window.location.href = "index.html"
        },
        exit() {
            axios({
                method: "post",
                url: "./user/exitServlet"
            }).then(function (resp) {
                if (resp.data === "ok") {
                    window.location.href = "login.html";
                }
            })
        },
        //修改用户名和密码
        changeUserPasswd() {
            _this = this;
            if (this.formInline.user != '' || this.formInline.password != ''){
                //避免全部传入空值
               if (this.formInline.user != '' && this.formInline.password == ''){
                   //修改用户名
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "username=" + this.formInline.user
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '修改用户名成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }else if (this.formInline.user == '' && this.formInline.password != ''){
                   //修改密码
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "password=" + this.formInline.password
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '修改密码成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }else if (this.formInline.user != '' && this.formInline.password != ''){
                   //修改用户名和密码
                   axios({
                       method: "post",
                       url: "./user/changeServlet",
                       data: "username=" + this.formInline.user + "&password=" + this.formInline.password
                   }).then(function (resp) {
                       if (resp.data == "ok") {
                           //提示
                           _this.$message({
                               message: '用户名和密码修改成功！',
                               type: 'success'
                           });
                           window.location.href = "login.html";
                       }
                   })
               }
            }else {
                //提示错误
                _this.$message.error('请输入要修改的用户名或密码');
            }
        },
        tableRowClassName({ row, rowIndex }) {
            if (rowIndex === 1) {
                return 'warning-row';
            } else if (rowIndex === 3) {
                return 'success-row';
            }
            return '';
        },
        //上传插件
        submitUpload() {
            var rs = this.$refs.upload1.submit().data;
            alert(rs)
        },
        //上传war包
        submitUpload2() {
            var rs = this.$refs.upload2.submit().data;
            alert(rs);
        },
        //更新系统
        updateSystem(){

        },
        handleRemove(file, fileList) {
            console.log(file, fileList);
        },
        handlePreview(file) {
            console.log(file);
        },
        handleResponse(response, file, fileList) {
            if (response == "uploadok") {
                this.$message({
                    message: '添加成功！',
                    type: 'success'
                });
                window.location.reload();
            } else {
                this.$message.error('添加失败！');
            }
        },
        handleRemove2(file, fileList) {
            console.log(file, fileList);
        },
        handlePreview2(file) {
            console.log(file);
        },
        handleResponse2(response, file, fileList) {
            if (response == "uploadok") {
                this.preparingForUpdates = !this.preparingForUpdates;
                this.$message({
                    message: '添加成功！',
                    type: 'success'
                });
            } else {
                this.$message.error('添加失败！');
            }
        },
        //重启系统
        restart() {
            this.$confirm('此操作将重启系统且该页面不会自动刷新, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/controlSystemServlet",
                    data: "controlCode=1"
                })
                this.$message({
                    type: 'success',
                    message: '正在重启中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消重启'
                });
            });
        },
        //更新系统
        updateSystem() {
            this.$confirm('此操作将更新系统且该页面不会自动刷新, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/controlSystemServlet",
                    data: "controlCode=2"
                })
                this.$message({
                    type: 'success',
                    message: '正在更新系统中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消重启'
                });
            });
        },
        toggleSelection(rows) {
            if (rows) {
                rows.forEach(row => {
                    this.$refs.multipleTable.toggleRowSelection(row);
                });
            } else {
                this.$refs.multipleTable.clearSelection();
            }
        },
        handleSelectionChange(val) {
            for (let i = 0; i < val.length; i++) {
                var name = val[i].name;
                var version = val[i].version;
                var info = "name=" + name + "&version=" + version;
                this.multipleSelection[i] = info;
            }
        },
        //批量删除
        deletePlugins() {
            this.$confirm('此操作将永久删除选择的插件且系统会重启, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                for (let i = 0; i < this.multipleSelection.length; i++) {
                    axios({
                        method: "post",
                        url: "./system/deletePluginServlet",
                        data: this.multipleSelection[i]
                    })
                }
                this.$message({
                    type: 'success',
                    message: '插件正在删除中...该页面不会自动刷新'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //单个删除
        deletePlugin(row) {
            this.$confirm('此操作将永久删除该插件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios({
                    method: "post",
                    url: "./system/deletePluginServlet",
                    data: "name=" + row.name + "&version=" + row.version
                })
                this.$message({
                    type: 'success',
                    message: '插件正在删除中...该页面不会自动刷新!'
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        }
    },
});