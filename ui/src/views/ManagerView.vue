<template>
  <div id="app">
    <!--面包屑-->
    <div class="sidebar">

      <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
        <el-submenu index="system">
          <template slot="title">项目</template>
          <el-menu-item index="info">概况</el-menu-item>
          <el-menu-item index="logs">实时日志</el-menu-item>
          <el-menu-item index="historyLogs">历史日志</el-menu-item>
        </el-submenu>

        <el-submenu index="user">
          <template slot="title">用户</template>
          <el-menu-item index="changePassword">修改</el-menu-item>
          <el-menu-item index="dataMigration">数据迁移</el-menu-item>
          <el-menu-item index="customDomainName">自定义域名</el-menu-item>
          <el-menu-item index="certificates">开启https</el-menu-item>
          <el-menu-item index="path">设置访问路径</el-menu-item>
          <el-menu-item index="alist">alist</el-menu-item>
          <el-menu-item index="ipAddressFilter">地址过滤</el-menu-item>
          <el-menu-item index="api">api</el-menu-item>
          <el-menu-item index="telegramBot">telegramBot</el-menu-item>
          <el-menu-item index="other">其他</el-menu-item>
        </el-submenu>

        <el-submenu index="download">
          <template slot="title">下载 | 上传</template>
          <el-menu-item index="Downloading">正在下载&nbsp;<span v-if="download.progress.length > 0">{{
              download.progress.length
            }}</span></el-menu-item>
          <el-menu-item index="Done">下载 | 上传已完成&nbsp;<span v-if="download.done.length > 0">{{
              download.done.length
            }}</span></el-menu-item>
          <el-menu-item index="error">下载 | 上传错误&nbsp;<span v-if="download.error.length > 0">{{
              download.error.length
            }}</span></el-menu-item>
          <el-menu-item index="DownloaderInfo">下载器信息</el-menu-item>
        </el-submenu>

        <el-submenu index="plugin">
          <template slot="title">插件</template>
          <el-menu-item index="pluginSettings">插件设置</el-menu-item>
          <el-menu-item index="pluginList">插件列表</el-menu-item>
          <el-menu-item index="pluginUpdate">插件更新</el-menu-item>
        </el-submenu>

        <el-menu-item @click="toSubList">订阅列表</el-menu-item>
        <el-menu-item index="about">关于</el-menu-item>
        <el-menu-item index="logout" @click="logout()">退出</el-menu-item>
      </el-menu>

      <!-- 信息对话框 -->
      <el-dialog title="详细" :visible.sync="download.downloadDetailVisible" width="30%">
        <el-form ref="form" :model="download.detail" label-width="auto">
          <el-form-item label="下载时间">
            <div @click="copy(download.detail.createTime)">
              <el-input v-model="download.detail.createTime"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="状态">
            <div @click="copy(download.detail.status)">
              <el-input v-model="download.detail.status"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="下载器">
            <div @click="copy(download.detail.downloaderName)">
              <el-input v-model="download.detail.downloaderName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="下载器版本">
            <div @click="copy(download.detail.downloaderVersion)">
              <el-input v-model="download.detail.downloaderVersion"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="文件名称">
            <div @click="copy(download.detail.fileName)">
              <el-input v-model="download.detail.fileName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="频道名称">
            <div @click="copy(download.detail.channelName)">
              <el-input v-model="download.detail.channelName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="节目标题">
            <div @click="copy(download.detail.itemTitle)">
              <el-input v-model="download.detail.itemTitle"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="节目链接">
            <div @click="copy(download.detail.itemLink)">
              <el-input v-model="download.detail.itemLink"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="订阅链接">
            <div @click="copy(download.detail.subLink)">
              <el-input v-model="download.detail.subLink"></el-input>
            </div>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!-- 插件设置 -->
      <el-dialog title="设置" :visible.sync="plugin.settingsVisible" width="30%">
        <div v-if="plugin.settings.length > 0">
          <el-form ref="form" label-width="auto">
            <div v-for="(item, key) in plugin.settings" :key="key">
              <el-form-item :label="item.name">
                <el-input v-model="item.content"></el-input>
                <el-tooltip class="item" effect="dark" :content="item.tip" placement="top-start">
                  <i class="el-icon-question"></i>
                </el-tooltip>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <span v-if="plugin.settings.length == 0">暂无设置</span>
        <span slot="footer" class="dialog-footer">
                    <el-button @click="plugin.settingsVisible = false">取 消</el-button>
                    <el-button type="primary" @click="updatePluginSettings()">确 定</el-button>
                </span>
      </el-dialog>
    </div>

    <!--查看实时日志-->
    <div class="main">
      <div v-show="activeMenu === 'logs'">
        <title>实时日志页面</title>
        <div id="logspage">
          <el-card>
            <div slot="header" class="clearfix">
              <span>实时日志</span>
              <el-button style="float: right;" type="danger" @click="clearLogs">清除日志</el-button>
            </div>
            <div class="logs">
              <div v-for="log in system.logs" :key="log.id">
                <LogMessage :message="formatLog(log)"/>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!--查看历史日志-->
      <div class="main">
        <div v-show="activeMenu === 'historyLogs'">
          <title>历史日志页面</title>
          <div id="historyLogspage">
            <el-card>
              <div slot="header" class="clearfix">
                <!-- 自定义日志时间范围 -->
                <el-date-picker
                    v-model="system.selectHistoryByTime"
                    type="datetimerange"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    :default-time="['00:00:00']"
                    format="yyyy-MM-dd HH:mm:ss"
                    value-format="yyyy-MM-dd HH:mm:ss"
                    align="center"
                    v-if="system.historyLogsLatest == -1">
                </el-date-picker>
                <span>&nbsp;</span>
                <el-select v-model="system.historyLogsLatest" placeholder="查看最近">
                  <el-option label="最近10分钟" value="10"></el-option>
                  <el-option label="最近20分钟" value="20"></el-option>
                  <el-option label="最近30分钟" value="30"></el-option>
                  <el-option label="最近60分钟" value="60"></el-option>
                  <el-option label="最近120分钟" value="120"></el-option>
                  <el-option label="自定义" value="-1"></el-option>
                </el-select>
                <span>&nbsp;</span>
                <el-select v-model="system.historyLogsLevel" placeholder="级别">
                  <el-option label="error" value="error"></el-option>
                  <el-option label="info" value="info"></el-option>
                </el-select>
                <span>&nbsp;</span>
                <el-button type="primary" plain v-if="system.historyLogsLatest != -1" @click="getHistoryLogs()">查看
                </el-button>
                <span>&nbsp;</span>

                <span>&nbsp;</span>
                <el-button type="primary" v-if="system.selectHistoryByTime.length==2 && system.historyLogsLatest == -1"
                           @click="getHistoryLogsByTime()">查看
                </el-button>
                <span>&nbsp;</span>

                <el-button type="primary" v-if="system.historyLogs.length != 0" @click="downloadLogs()">下载日志
                </el-button>

                <el-button style="float: right;" type="danger" @click="clearHistoryLogs">清除日志</el-button>
              </div>
              <div class="logs">
                <div v-for="log in system.historyLogs" :key="log.id">
                  <LogMessage :message="formatLog(log)"/>
                </div>
              </div>
            </el-card>
          </div>
        </div>


        <!--显示项目介绍-->
        <div v-show="activeMenu === 'about'">
          <el-menu>
            <el-menu-item>
              <iframe src="https://yajuhua.github.io" width="100%" style="height: 95vh;"
                      frameborder="0"></iframe>
            </el-menu-item>
          </el-menu>
        </div>


        <!--修改用户名和密码-->
        <div v-show="activeMenu === 'changePassword'">
          <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="用户名">
              <el-input v-model="user.account.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="user.account.password" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="userAccountChange()">修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!--自定义域名-->
        <div v-show="activeMenu === 'customDomainName'">
          <el-form :inline="true" class="demo-form-inline">

            <el-form-item label="附件域名">
              <el-input v-model="user.domain.value" placeholder="请输入附件域名"></el-input>
              <el-tooltip class="item" effect="dark" content="如https://yajuhua.github.io:8088"
                          placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changeDomain()">修改</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="deleteDomain()">删除</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 开启https -->
        <div v-show="activeMenu === 'certificates'">
          <div v-if="user.cert.list.length > 0" @click="switchSsl()">
            <el-switch v-model="user.cert.switchSsl" active-text="开启"></el-switch>
          </div>
          <br>
          <el-upload class="upload-demo" ref="uploadCertFile" action="/user/cert"
                     :on-change="certFilehandleChange" :on-remove="certFilehandleRemove" :file-list="user.cert.fileList"
                     :auto-upload="false" :multiple="true" name="files" v-if="user.cert.list.length == 0">
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success"
                       @click="uploadCert">上传到服务器
            </el-button>
            <div slot="tip" class="el-upload__tip">只能上传crt和key文件</div>
          </el-upload>
          <el-table ref="multipleTable" :data="user.cert.list" tooltip-effect="dark" style="width: 100%">
            <el-table-column>
              <el-tag>crt</el-tag>
              <el-tag type="success">key</el-tag>
            </el-table-column>

            <el-table-column label="操作" width="180">
              <el-button type="danger" @click="deleteCert()">删除</el-button>
            </el-table-column>
          </el-table>

        </div>

        <!--设置访问路径-->
        <div v-show="activeMenu === 'path'">
          <el-form :inline="true" class="demo-form-inline">

            <el-form-item label="访问路径">
              <el-input v-model="user.path" placeholder="请输入路径"></el-input>
              <el-tooltip class="item" effect="dark" content="如果设置为podcast2,那么面板访问路径/p/podcast2"
                          placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePath()">修改</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="deletePath()">删除</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!--设置alist-->
        <div v-show="activeMenu === 'alist'">
          <el-form :inline="true" class="demo-form-inline" label-width="auto">

            <el-form-item label="链接">
              <el-tooltip class="item" effect="dark" content="如：http://192.168.123.3:5244"
                          placement="top-start">
                <el-input v-model="user.submitAlist.url" :placeholder="user.alistInfo.url" size="medium"
                          :style="{ width: '300px' }" clearable></el-input>
              </el-tooltip>
            </el-form-item>
            <br/>

            <el-form-item label="用户名">
              <el-input v-model="user.submitAlist.username" :placeholder="user.alistInfo.username" size="medium"
                        :style="{ width: '300px' }" clearable></el-input>
            </el-form-item>
            <br/>

            <el-form-item label="密码">
              <el-input v-model="user.submitAlist.password" :placeholder="user.alistInfo.password" size="medium"
                        :style="{ width: '300px' }" clearable></el-input>
            </el-form-item>
            <br/>

            <el-form-item label="目录">
              <el-tooltip class="item" effect="dark" content="如：/podcast2是alist根目录下的podcast2文件夹"
                          placement="top-start">
                <el-input v-model="user.submitAlist.path" :placeholder="user.alistInfo.path" size="medium"
                          :style="{ width: '300px' }" clearable></el-input>
              </el-tooltip>
            </el-form-item>
            <br/>

            <el-form-item>
              <el-button type="primary" @click="openAlist()" v-if="user.alistInfo.open==false">开启</el-button>
              <el-button type="danger" @click="closeAlist()" v-if="user.alistInfo.open==true">关闭</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!--设置telegramBot-->
        <div v-show="activeMenu === 'telegramBot'">
          <el-form :inline="true" class="demo-form-inline" label-width="auto">

            <el-form-item label="username">
              <el-tooltip class="item" effect="dark" content="如：xxx_bot"
                          placement="top-start">
                <el-input v-model="user.botInfo.username" size="medium"
                          :style="{ width: '300px' }" clearable></el-input>
              </el-tooltip>
            </el-form-item>
            <br/>

            <el-form-item label="token">
              <el-input v-model="user.botInfo.token" size="medium"
                        :style="{ width: '300px' }" clearable></el-input>
            </el-form-item>
            <br/>

            <el-form-item label="代理">
              <el-tooltip class="item" effect="dark" content="如：http://127.0.0.1:10809"
                          placement="top-start">
                <el-input v-model="user.botInfo.proxy" size="medium"
                          :style="{ width: '300px' }" clearable></el-input>
              </el-tooltip>
            </el-form-item>
            <br/>
            <el-form-item label="开关">
              <el-select v-model="user.botInfo.isOpen">
                <el-option label="开启" value="true"></el-option>
                <el-option label="关闭" value="false"></el-option>
              </el-select>
            </el-form-item>
            <br/>

            <el-form-item>
              <el-button type="primary" @click="updateBotInfo()">修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!--设置地址过滤-->
        <div v-show="activeMenu === 'ipAddressFilter'">
          <el-form ref="blacklistForm" label-width="100px" class="demo-dynamic">
            <el-form-item
                v-for="(ip, index) in user.ipAddressFilter.blacklist"
                :label="'黑IP地址' + (index + 1)"
                :key="ip.key"
                :prop="user.ipAddressFilter.blacklist[index]"
                :rules="[{ required: true, message: '不能为空', trigger: 'blur' }]">
              <el-tooltip class="item" effect="dark" content="支持单个IP和IP范围（192.168.123.1-192.168.123.100）" placement="right">
              <el-input v-model="user.ipAddressFilter.blacklist[index]" style="width: 300px;" maxlength="31" minlength="15"></el-input>
              </el-tooltip>
              <span>&nbsp;</span>
              <el-button @click.prevent="removeBlackIp(ip)">删除</el-button>
            </el-form-item>
          </el-form>
          <el-form ref="whitelistForm" label-width="100px" class="demo-dynamic">
            <el-form-item
                v-for="(ip, index) in user.ipAddressFilter.whitelist"
                :label="'白IP地址' + (index + 1)"
                :key="ip.key"
                :prop="user.ipAddressFilter.whitelist[index]"
                :rules="[{ required: true, message: '不能为空', trigger: 'blur' }]">
              <el-tooltip class="item" effect="dark" content="支持单个IP和IP范围（192.168.123.1-192.168.123.100）" placement="right">
              <el-input v-model="user.ipAddressFilter.whitelist[index]" style="width: 300px;" maxlength="31" minlength="15"></el-input>
              </el-tooltip>
              <span>&nbsp;</span>
              <el-button @click.prevent="removeWhiteIp(ip)">删除</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitWhiteBlacklist()">提交</el-button>
              <el-button @click="addBlacklist()" type="info">新增黑名单</el-button>
              <el-button @click="addWhitelist()">新增白名单</el-button>
              <el-button @click="clearWhiteBlacklist()" type="warning">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!--api-->
        <div v-show="activeMenu === 'api'">
          <div>
            <el-input v-model="user.apiToken.apiToken" @click.native="copy(user.apiToken.apiToken)">
              <template slot="prepend">apiToken</template>
            </el-input>
            <el-row style="margin-top: 15px;">
              <el-button type="primary" v-if="user.apiToken.hasApiToken == false" @click="createApiToken()">点击生成</el-button>
              <el-button type="danger" v-if="user.apiToken.hasApiToken == true" @click="removeApiToken()">点击删除</el-button>
            </el-row>
          </div>
          <div style="margin-top: 15px;">
            <el-form>
              <el-form-item label="api文档">
                  <a :href="user.currentHost + '/doc.html'" target="_blank">{{ user.currentHost + '/doc.html' }}</a>
              </el-form-item>
              <el-form-item label="开关">
                <el-select v-model="user.apiDoc.status">
                  <el-option label="开启" value="true"></el-option>
                  <el-option label="关闭" value="false"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                  <el-button type="primary" @click="updateApiDocStatus()">修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!--其他设置-->
        <div v-show="activeMenu === 'other'">
          <!--设置Github加速站-->
          <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="Github加速站">
              <el-input v-model="user.githubProxyUrl" placeholder="请输入Github加速站"></el-input>
              <el-tooltip class="item" effect="dark"
                          content="国内无法直接通过yt-dlp更新需要设置Github加速站,当然代理除外。"
                          placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateGithubProxyUrl()">修改</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="deleteGithubProxyUrl()">删除</el-button>
            </el-form-item>
          </el-form>
          <!--自定义插件仓库链接-->
          <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="自定义插件仓库">
              <el-input v-model="user.pluginUrl" placeholder="请输入插件仓库链接"></el-input>
              <el-tooltip class="item" effect="dark" content="若内置插件仓库无法访问可以自定义"
                          placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePluginUrl()">修改</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="deletePluginUrl()">删除</el-button>
            </el-form-item>
          </el-form>
        </div>


        <!--数据迁移-->
        <div v-show="activeMenu === 'dataMigration'">
          <el-button type="success" round @click="dataExport()">导出</el-button>
          <el-button type="primary" round @click="dataImport()">
            导入
            <!--ref属性用于重置文件，避免浏览器认为是同一个文件-->
            <input type="file" class="custom-file-input" @change="handleFileSelect" ref="fileInput"/>
          </el-button>
          <el-table ref="multipleTable" :data="user.dataMigration.subData" tooltip-effect="dark"
                    style="width: 100%" @selection-change="dataExportHandleSelectionChange">
            <el-table-column type="selection" width="55">
            </el-table-column>
            <el-table-column type="index" width="50">
            </el-table-column>
            <el-table-column label="更新时间" width="120" prop="updateTime">
            </el-table-column>
            <el-table-column label="频道名称" prop="title" show-overflow-tooltip>
            </el-table-column>
          </el-table>

          <!-- 选择导入订阅 -->
          <el-dialog title="导入" :visible.sync="user.dataMigration.dataImportVisible" width="40%">
            <div>
              <el-table ref="multipleTable" :data="user.dataMigration.import" tooltip-effect="dark"
                        style="width: 100%" @selection-change="dataImportHandleSelectionChange">
                <el-table-column type="selection" width="55">
                </el-table-column>
                <el-table-column type="index" width="50">
                </el-table-column>
                <el-table-column label="频道名称" prop="sub.title" show-overflow-tooltip>
                </el-table-column>
              </el-table>
            </div>
            <span slot="footer" class="dialog-footer">
                        <el-button @click="user.dataMigration.dataImportVisible = false">取 消</el-button>
                        <el-button type="primary" @click="dataImportToServer()">导 入</el-button>
                    </span>
          </el-dialog>

        </div>


        <!--查看正在下载-->
        <div v-show="activeMenu === 'Downloading'">
          <el-table :data="download.progress" stripe style="width: 100%">
            <el-table-column prop="channelName" label="频道名称" width="180">
            </el-table-column>
            <el-table-column prop="itemName" label="节目名称" width="180">
            </el-table-column>
            <el-table-column label="进度">
              <el-progress :text-inside="true" :stroke-width="26" slot-scope="scope"
                           :percentage="scope.row.downloadProgress"></el-progress>
            </el-table-column>
            <el-table-column prop="downloadSpeed" label="速度">
            </el-table-column>
            <el-table-column prop="downloadTimeLeft" label="剩余时间">
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="scope">
                <el-button type="primary" @click="downloadDetail(scope.row.uuid)">详细</el-button>
                <el-button type="danger"
                           @click="deleteDownloading(scope.row.uuid)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!--查看已完成-->
        <div v-show="activeMenu === 'Done'">
          <div style="height: 90vh; overflow-y: scroll">
            <el-button type="danger" round @click="batchDeleteDownloadDone()"
                       v-if="download.selectionDownloadDone.length > 1">批量删除
            </el-button>
            <el-button type="primary" round @click="getDownloadDone()">刷新列表</el-button>
            <el-table :data="download.done" stripe style="width: 100%" ref="selectDownloadDone"
                      @selection-change="handleSelectionDownloadDone">
              <el-table-column
                  type="selection"
                  width="55">
              </el-table-column>
              <el-table-column
                  type="index"
                  width="50">
              </el-table-column>
              <el-table-column label="状态" width="180">
                <i slot-scope="scope"
                   :class="{ 'el-icon-success': scope.row.status === 5 || scope.row.status === 24 , 'el-icon-error': scope.row.status !== 5 && scope.row.status !== 24 }"
                   :style="{ fontSize: '45px', color: scope.row.status === 5 || scope.row.status === 24 ? '#54AC1C' : '#F95C61'  }">
                </i>
              </el-table-column>
              <el-table-column prop="channelName" label="频道名称" width="180">
              </el-table-column>
              <el-table-column prop="itemName" label="节目标题">
              </el-table-column>
              <el-table-column label="进度">
                <el-progress :status="scope.row.status != '5' && scope.row.status != '24' ? 'exception' : 'success'"
                             :text-inside="true"
                             :stroke-width="26" slot-scope="scope" :percentage="scope.row.downloadProgress">
                </el-progress>
              </el-table-column>
              <el-table-column label="操作">
                <div slot-scope="scope">
                  <el-button type="success" @click="reDownload(scope.row.uuid)">重新</el-button>
                  <el-button type="primary" @click="downloadDetail(scope.row.uuid)">详细</el-button>
                  <el-button type="danger" @click="downloadDelete(scope.row.uuid)">删除</el-button>
                </div>

              </el-table-column>
            </el-table>
          </div>
        </div>

        <!--查看上传&下载错误-->
        <div v-show="activeMenu === 'error'">
          <div style="height: 90vh; overflow-y: scroll">
            <el-table :data="download.error" stripe style="width: 100%">
              <el-table-column label="状态" width="180">
                <i slot-scope="scope"
                   :class="{ 'el-icon-success': scope.row.status === 5 || scope.row.status === 24 , 'el-icon-error': scope.row.status !== 5 && scope.row.status !== 24 }"
                   :style="{ fontSize: '45px', color: scope.row.status === 5 || scope.row.status === 24 ? '#54AC1C' : '#F95C61'  }">
                </i>
              </el-table-column>
              <el-table-column prop="channelName" label="频道名称" width="180">
              </el-table-column>
              <el-table-column prop="itemName" label="节目标题">
              </el-table-column>
              <el-table-column label="进度">
                <el-progress :status="scope.row.status != '5' && scope.row.status != '24' ? 'exception' : 'success'"
                             :text-inside="true"
                             :stroke-width="26" slot-scope="scope" :percentage="scope.row.downloadProgress">
                </el-progress>
              </el-table-column>
              <el-table-column label="操作">
                <div slot-scope="scope">
                  <el-button type="success" @click="reDownload(scope.row.uuid)">重新</el-button>
                  <el-button type="primary" @click="downloadDetail(scope.row.uuid)">详细</el-button>
                  <el-button type="danger" @click="downloadDelete(scope.row.uuid)">删除</el-button>
                </div>

              </el-table-column>
            </el-table>
          </div>
        </div>

        <!--查看下载器信息-->
        <div v-show="activeMenu === 'DownloaderInfo'">
          <el-table :data="download.info" stripe style="width: 100%">
            <el-table-column prop="name" label="名称">
            </el-table-column>
            <el-table-column prop="version" label="版本">
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间">
            </el-table-column>
          </el-table>
        </div>

        <!--插件设置-->
        <div v-show="activeMenu === 'pluginSettings'">
          <div @click="autoUpdatePlugin()">
            <el-switch v-model="plugin.autoUpdate" active-text="自动更新"></el-switch>
          </div>

        </div>

        <!-- 插件列表 -->
        <div v-show="activeMenu === 'pluginList'">

          <!-- 插件详细信息 -->
          <el-dialog title="详细信息" :visible.sync="plugin.detailVisible" width="30%">
            <el-table :data="plugin.detail" stripe style="width: 100%">
              <el-table-column prop="name">
              </el-table-column>
              <el-table-column prop="content">
              </el-table-column>
            </el-table>
            <span slot="footer" class="dialog-footer">
                        <el-button type="primary" @click="plugin.detailVisible = false">确 定</el-button>
                    </span>
          </el-dialog>

          <el-input v-model="plugin.search" style="width: 20%" placeholder="搜索插件"></el-input>&nbsp;
          <el-button type="primary" @click="pluginSearch()" :icon="plugin.searchIng">搜索</el-button>
          <el-button type="primary" @click="getPluginList()">全部</el-button>
          <br><br>
          <el-upload class="upload-demo" ref="upload" :auto-upload="false" :limit="1">
            <el-button slot="trigger" size="small" type="primary">上传本地插件</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success"
                       @click="submitUpload">上传到服务器
            </el-button>
            <el-button style="margin-left: 10px;" size="small" type="primary"
                       @click="getPluginList()">刷新插件列表
            </el-button>
            <div slot="tip" class="el-upload__tip">只能上传jar文件</div>
          </el-upload>
          <el-table :data="plugin.list" stripe style="width: 100%">
            <el-table-column prop="name" label="名称" width="180">
            </el-table-column>
            <el-table-column prop="version" label="版本" width="180">
            </el-table-column>
            <el-table-column prop="update" label="更新时间">
            </el-table-column>
            <el-table-column prop="keyInfo" label="提醒">
            </el-table-column>
            <el-table-column fixed="right" label="操作">
              <div slot-scope="scope">
                <div v-if="scope.row.install === true && !scope.row.hasUpdate">
                  <!-- 已经安装的，可卸载 -->
                  <el-button type="danger" @click="pluginDelete(scope.row.uuid)">卸载</el-button>
                  <el-button type="primary" @click="pluginDetail(scope.row.uuid)">详细</el-button>
                  <el-button type="info" @click="getPluginSettings(scope.row.name)">设置</el-button>
                </div>
                <div v-if="scope.row.install === false">
                  <el-button type="success" @click="pluginInstall(scope.row.uuid)"
                             :ref="scope.row.uuid">安装
                  </el-button>
                  <el-button type="primary" @click="pluginDetail(scope.row.uuid)">详细</el-button>
                </div>
                <div v-if="scope.row.hasUpdate === true && scope.row.install === true">
                  <el-button type="danger" @click="pluginDelete(scope.row.uuid)">卸载</el-button>
                  <el-button type="primary" @click="pluginDetail(scope.row.uuid)">详细</el-button>
                  <el-button type="info" @click="getPluginSettings(scope.row.name)">设置</el-button>
                  <el-button type="warning" :ref="scope.row.name"
                             @click="pluginUpdate(scope.row.name)">更新
                  </el-button>
                </div>

              </div>
            </el-table-column>
          </el-table>
        </div>

        <!-- 插件更新 -->
        <div v-show="activeMenu === 'pluginUpdate'">
          <el-table :data="plugin.updateList" stripe style="width: 100%" ref="multipleTable"
                    @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55">
            </el-table-column>
            <el-table-column prop="name" label="名称" width="180">
            </el-table-column>
            <el-table-column prop="version" label="版本" width="180">
            </el-table-column>
            <el-table-column prop="update" label="更新时间">
            </el-table-column>
            <el-table-column fixed="right" label="操作">
              <el-button type="warning" slot-scope="scope" @click="pluginUpdate(scope.row.name)"
                         :ref="scope.row.name">更新
              </el-button>
            </el-table-column>
          </el-table>
          <div class="footer">
            <el-button v-if="plugin.updateNames.length > 1" type="warning"
                       @click="pluginBatchUpdate">批量更新
            </el-button>
          </div>
        </div>

        <!--项目概况-->
        <div v-show="activeMenu === 'info'">
          <h2>项目概况</h2>
          <el-button type="danger" @click="restart()">重启</el-button>&nbsp;<el-button type="primary"
                                                                                       @click="checkForUpdate()">检查更新
        </el-button>
          <el-table :data="system1.info" stripe style="width: 100%">
            <el-table-column prop="name">
            </el-table-column>
            <el-table-column prop="content">
            </el-table-column>
          </el-table>
        </div>

        <!-- 检查更新窗口 -->
        <div>
          <el-dialog
              title="检查更新"
              :visible.sync="system.update.windowsVisible"
              width="50%"
              v-loading="system.update.windowsLoading"
              :before-close="handleCloseUpdateWindow"
              :element-loading-text="system.update.loadingTip">
            <div v-html="system.update.info.desc"></div>
           <span slot="footer" class="dialog-footer">
            <el-button @click="system.update.windowsVisible = false">关闭窗口</el-button>
            <el-button type="primary" v-if="system.update.info.hasUpdate && !system.update.status.download && !system.update.status.downloading" @click="downloadJarFile()">下载最新Jar包</el-button>
            <el-button type="primary" :loading="system.update.status.downloading" v-if="system.update.status.downloading">下载中 {{system.update.status.percent}}%</el-button>
            <el-button type="primary" v-if="system.update.status.downloading" @click="cancelDownloadJarFile()">取消下载</el-button>
            <el-button v-if="system.update.status.download" type="danger" @click="deleteDownloadLatestJarFile()">删除</el-button>
            <el-button v-if="system.update.status.download" type="primary" @click="restart()">立即重启</el-button>
          </span>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import LogMessage from "@/views/LogMessage";

export default {
  components: {LogMessage},
  data() {
    return {
      activeMenu: 'info',
      plugin: {
        list: [],
        installStatus: '',
        search: '',
        searchIng: '',
        autoUpdate: true,
        uploadFileList: [],
        detail: [],
        detailVisible: false,
        updateList: [],
        updateNames: [],
        settings: [],
        settingsVisible: false
      },
      download: {
        info: [],
        progress: [],
        done: [],
        detail: {},
        downloadDetailVisible: false,
        error: [],
        selectionDownloadDone: []
      },
      system: {
        logs: [],
        historyLogs: [],
        historyLogsLatest: 10,
        historyLogsLevel: 'error',
        selectHistoryByTime: [],
        //在线更新
        update: {
          info: {
            hasUpdate: false,
            version: '',
            desc: ''
          },
          status:{
            percent: 0.0,
            completed: false,
            error: false,
            download: false,
            downloading: true,
            version: ''
          },
          windowsVisible: false,
          windowsLoading: false,
          loadingTip: ''
        }
      },
      user: {
        domain: {
          value: '',
          select: '0'
        },
        account: {
          username: '',
          password: ''
        },
        dataMigration: {
          import: [],
          export: [],
          subData: [],
          dataImportVisible: false,
          selectImport: []
        },
        cert: {
          fileList: [],
          fileFormList: [],
          list: [],
          switchSsl: false
        },
        path: '',
        githubProxyUrl: '',
        pluginUrl: '',
        submitAlist: {
          url: '',
          username: '',
          password: '',
          path: '',
          open: false
        },
        alistInfo: {
          url: '',
          username: '',
          password: '',
          path: '',
          open: false
        },
        ipAddressFilter: {
          whitelist: [],
          blacklist: []
        },
        apiToken:{
          hasApiToken: false,
          apiToken: ''
        },
        botInfo: {
          username: null,
          isOpen: 'false',
          token: null,
          proxy: null
        },
        apiDoc: {
          status: false
        },
        currentHost: window.location.origin
      },
      system1: {
        info: []
      }

    }
  },
  mounted() {
    //获取插件列表
    this.getPluginList();
    //获取插件自动更新状态
    this.getAutoUpdateStatus();
    //获取插件更新列表
    this.pluginUpdateList();
    //获取下载器信息
    this.getDownloaderInfo();
    //开启下载器进度ws
    this.setupDownloadSocket();
    //获取下载完成信息
    this.getDownloadDone();
    //开启日志ws
    this.setupLogsSocket();
    //获取订阅列表数据
    this.getSubList();
    //获取系统信息
    this.getSystemInfo();
    //获取ssl列表
    this.getSslList();
    //获取ssl状态
    this.getSslStatus();
    //获取面板访问路径
    this.getPath();
    //获取下载错误信息
    this.getDownloadError();
    //获取Github加速站
    this.getGithubProxyUrl();
    //获取插件仓库链接
    this.getPluginUrl();
    //获取alist配置信息
    this.getAlistInfo();
    //获取黑白名单
    this.getAddressFilter();
    //获取botInfo
    this.getBotInfo();
    //获取apiToken
    this.getApiTokenInfo();
    //获取api文档状态
    this.getApiDocStatus();
    //获取附件自定义域名
    this.getEnclosureDomain();
  },
  methods: {
    toSubList() {
      this.$router.push('/')
    },
    handleMenuSelect(index) {
      this.activeMenu = index;
    },
    //卸载插件
    pluginDelete(uuid) {
      this.$confirm('此操作将永久删除该插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/plugin?uuids=' + uuid)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success("插件删除成功！");
                this.getPluginList();

              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          this.$message.error("插件删除错误！")
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取插件列表
    getPluginList() {
      axios.get('/plugin/list')
          .then(res => {
            if (res.data.code == '1') {
              this.plugin.list = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err)
        this.$message.error("获取插件列表失败");
      })
    },
    //安装插件
    pluginInstall(uuid) {
      const butRef = this.$refs[uuid];
      if (butRef.icon == 'el-icon-loading') {
        this.$message.warning('正在安装中请稍等片刻...')
        return
      }
      this.$confirm('此操作将安装该插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        butRef.icon = 'el-icon-loading';
        axios.get('/plugin/install?uuids=' + uuid)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('插件安装成功！');
                this.getPluginList();
              } else {
                this.$message.error(res.data.msg);
              }
              butRef.icon = '';
            }).catch(err => {
          this.$message.error('插件安装失败！');
          console.log(err);
          butRef.icon = '';
        });

        // 定时查询安装状态
        const intervalId = setInterval(() => {
          axios.get('/plugin/install/status/' + uuid)
              .then(res => {
                if (res.data.data.install === true) {
                  this.plugin.installStatus = ''; // 清除安装状态
                  clearInterval(intervalId); // 停止定时查询
                  butRef.icon = '';
                }
              }).catch(err => {
            butRef.icon = '';
            console.error('查询安装状态失败：', err);
            clearInterval(intervalId); // 停止定时查询
          });
        }, 2000); // 每2秒查询一次安装状态
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });

    },
    //搜索插件
    pluginSearch() {
      if (this.plugin.search == '') {
        this.$message.error('请输入')
        return;
      }
      this.plugin.searchIng = 'el-icon-loading'
      axios.get('/plugin/search?keyword=' + this.plugin.search)
          .then(res => {
            if (res.data.code == '1') {
              this.plugin.list = res.data.data;
            } else {
              this.$message.error('搜索异常!');
            }
            this.plugin.searchIng = ''
          }).catch(err => {
        this.$message.error('搜索错误！')
        console.log(err)
        this.plugin.searchIng = ''
      })

    },
    //设置插件自动更新
    autoUpdatePlugin() {
      let status = 0;
      if (this.plugin.autoUpdate) {
        status = 1;
      }
      axios.post('/plugin/autoUpdate?status=' + status)
          .then(res => {
            if (res.data.code == '1') {
              if (status == '1') {
                this.plugin.autoUpdate = true;
              } else {
                this.plugin.autoUpdate = false;
              }
              console.log('修改成功！')
            } else {
              this.$message.error('修改错误！')
            }
          }).catch(err => {
        this.$message.error('修改错误！');
        console.log(err)
      })
    },
    getAutoUpdateStatus() {
      axios.get('/plugin/autoUpdate')
          .then(res => {
            if (res.data.code == '1') {
              if (res.data.data == '1') {
                this.plugin.autoUpdate = true;
              } else {
                this.plugin.autoUpdate = false;
              }
            } else {
              this.$message.error('获取插件自动更新状态失败！');
            }
          }).catch(err => {
        this.$message.error('获取插件自动更新状态失败！');
        console.log(err);
      })
    },
    //上传插件
    submitUpload() {
      let name = this.$refs.upload.uploadFiles[0].name;
      let s = name.split(".");
      let ext = s[s.length - 1];
      console.log('文件格式' + ext)
      if (ext == 'jar') {
        //构建一个表单把文件传进去
        let param = new FormData()
        param.append("files", this.$refs.upload.uploadFiles[0].raw)
        axios.post('/common/upload/plugin', param, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then(res => {
          if (res.data.code == '1') {
            this.$message.success('上传插件成功');
            this.$refs.upload.uploadFiles = [];
            this.getPluginList();
          } else {
            this.$message.error(res.data.msg);
          }

        })
            .catch(error => {
              console.error(error);
            });
      } else {
        this.$message.error('只能上传插件jar包！')
      }

    },
    //获取插件详细信息
    pluginDetail(uuid) {
      axios.get('/plugin/detail/' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.plugin.detail = res.data.data;
              this.plugin.detailVisible = true;
            } else {
              this.$message.warning('请先安装插件！')
            }
          }).catch(err => {
        console.log('获取插件详细信息失败!')
        console.log(err)
      })
    },
    //更新单个插件
    pluginUpdate(name) {
      const butRef = this.$refs[name];
      if (butRef.icon == 'el-icon-loading') {
        this.$message.warning('正在更新中...');
        return;
      }
      this.$confirm('此操作将更新该插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        butRef.icon = 'el-icon-loading';
        axios.post('/plugin/update', 'names=' + name)
            .then(res => {
              if (res.data.code == '1') {
                //重新获取插件更新列表
                this.$message.success('更新成功！')
                this.pluginUpdateList();
                this.getPluginList();
              } else {
                this.$message.error('更新插件错误！');
              }
              butRef.icon = '';
            }).catch(err => {
          butRef.icon = '';
          this.$message.error('更新插件错误！');
          console.log(err)
        })

        // 定时查询更新状态
        const intervalId = setInterval(() => {
          axios.get('/plugin/update/status/' + name)
              .then(res => {
                if (res.data.data.hasUpdate === false) {
                  clearInterval(intervalId); // 停止定时查询
                  butRef.icon = '';
                }
              }).catch(err => {
            butRef.icon = '';
            console.error('查询更新状态失败：', err);
            clearInterval(intervalId); // 停止定时查询
          });
        }, 2000); // 每2秒查询一次安装状态
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消更新'
        });
      });
      console.log(name)
    },
    //批量更新
    pluginBatchUpdate() {
      this.$confirm('此操作将批量更新插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let names = [];
        for (let index = 0; index < this.plugin.updateNames.length; index++) {
          names.push(this.plugin.updateNames[index].name)
        }
        axios.post('/plugin/update', 'names=' + names)
            .then(res => {
              if (res.data.code == '1') {
                //重新获取插件更新列表
                this.$message.success('更新成功！')
                this.pluginUpdateList();
                this.getPluginList();
              } else {
                this.$message.error('批量更新插件错误！');
              }
            }).catch(err => {
          this.$message.error('批量更新插件错误！');
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消批量更新'
        });
      });

    },
    //获取插件更新列表
    pluginUpdateList() {
      axios.get('/plugin/updateList')
          .then(res => {
            if (res.data.code == '1') {
              this.plugin.updateList = res.data.data;
              console.log(this.plugin.updateList)
            } else {
              this.$message.error('获取插件更新列表错误！');
            }
          }).catch(err => {
        this.$message.error('获取插件更新列表错误1');
        console.log(err);
      })
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
      this.plugin.updateNames = val;
    },
    //获取下载器信息
    getDownloaderInfo() {
      axios.get('/download/info')
          .then(res => {
            if (res.data.code == '1') {
              this.download.info = res.data.data;
            } else {
              this.$message.error('获取下载器信息失败！')
            }
          }).catch(err => {
        this.$message.error('获取下载器信息失败！')
        console.log(err)
      })
    },
    //下载进度展示
    setupDownloadSocket() {
      let clientId = Math.random().toString(36).substr(2);
      // let wsProtocol = window.location.protocol.includes("https") ? "wss" : "ws";
      // let wsHost = window.location.hostname; // 使用前端主机名
      // let wsPort = window.location.port ? `:${window.location.port}` : ''; // 使用前端端口，如果有的话
      //let wsPort = '8080';
      //let wsUrl = `${wsProtocol}://${wsHost}${wsPort}/ws/download/${clientId}`;
      let wsUrl = `/ws/download/${clientId}`;

      let websocket = null;
      //判断当前浏览器是否支持WebSocket
      if ('WebSocket' in window) {
        //连接WebSocket节点
        websocket = new WebSocket(wsUrl);
      } else {
        alert('Not support websocket')
      }

      //连接发生错误的回调方法
      websocket.onerror = function () {
        console.log('下载ws连接错误')
      };

      //连接成功建立的回调方法
      websocket.onopen = function () {
        console.log('下载ws连接成功')
      }

      //接收到消息的回调方法
      var vm = this; // 保存对Vue实例的引用
      websocket.onmessage = function (event) {
        let message = event.data;
        let object = JSON.parse(message);
        console.log(object);
        if (vm.download.progress.length != object.length) {
          //刷新
          vm.getDownloadDone();
          vm.getDownloadError();
        }
        vm.download.progress = object; // 使用vm代替this
      }


      //连接关闭的回调方法
      websocket.onclose = function () {
        console.log('下载ws关闭')
      }

      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function () {
        websocket.close();
      }
    },
    //日志
    setupLogsSocket() {
      let clientId = Math.random().toString(36).substr(2);
      // let wsProtocol = window.location.protocol.includes("https") ? "wss" : "ws";
      // let wsHost = window.location.hostname; // 使用前端主机名
      // let wsPort = window.location.port ? `:${window.location.port}` : ''; // 使用前端端口，如果有的话
      // let wsUrl = `${wsProtocol}://${wsHost}${wsPort}/ws/logs/${clientId}`;
      let wsUrl = `/ws/logs/${clientId}`;


      let websocket = null;
      //判断当前浏览器是否支持WebSocket
      if ('WebSocket' in window) {
        //连接WebSocket节点
        websocket = new WebSocket(wsUrl);
      } else {
        alert('Not support websocket')
      }

      //连接发生错误的回调方法
      websocket.onerror = function () {
        console.log('日志ws连接错误')
      };

      //连接成功建立的回调方法
      websocket.onopen = function () {
        console.log('日志ws连接成功')
      }

      //接收到消息的回调方法
      var vm = this; // 保存对Vue实例的引用
      websocket.onmessage = function (event) {
        let message = event.data;
        vm.system.logs.push(message);
      }


      //连接关闭的回调方法
      websocket.onclose = function () {
        console.log('日志ws关闭')
      }

      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function () {
        websocket.close();
      }
    },
    //获取下载完成的信息
    getDownloadDone() {
      axios.get('/download/completed')
          .then(res => {
            if (res.data.code == '1') {
              this.download.done = res.data.data;
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
        console.log(err)
        this.$message.error('获取下载完成的信息失败！')
      })
    },
    //获取下载错误的信息
    getDownloadError() {
      axios.get('/download/error')
          .then(res => {
            if (res.data.code == '1') {
              this.download.error = res.data.data;
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
        console.log(err)
        this.$message.error('获取下载完成的信息失败！')
      })
    },
    //获取下载详细信息
    downloadDetail(uuid) {
      this.download.detail = {}
      this.download.downloadDetailVisible = true
      axios.get('/download/detail/' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.download.detail = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        this.$message.error('获取下载详细信息失败！');
        console.log(err);
      })
    },
    //复制内容到粘贴板
    copy(content) {
      const textarea = document.createElement('textarea');

      console.log('复制到粘贴板')

      textarea.value = content;
      textarea.setAttribute('readonly', '');
      textarea.style.position = 'absolute';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);

      // 延迟显示复制成功提示
      setTimeout(() => {
        this.$message({
          message: '复制成功！',
          type: 'success'
        });
      }, 100);

      console.log('内容已成功复制到剪贴板');
    },
    //重新下载
    reDownload(uuid) {
      this.$confirm('此操作将重新处理该节目, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.get('/download/reDownload/' + uuid)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('正在重新处理中');
                //重新获取完成下载信息
                this.getDownloadDone();
              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          this.$message.error('重新处理失败！');
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消重新处理'
        });
      });

    },
    //清空日志
    clearLogs() {
      this.system.logs = [];
    },
    //清空历史日志
    clearHistoryLogs() {
      this.system.historyLogs = [];
    },
    //删除下载
    downloadDelete(uuid) {
      this.$confirm('此操作将删除该下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/download?uuids=' + uuid)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('删除成功！')
                this.getDownloadDone();
              } else {
                this.$message.error(res.data.msg)
              }
            }).catch(err => {
          console.log(err)
          this.$message.error('删除失败！')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    //修改附件域名
    changeDomain() {
      const domainRegex = /(http|https):\/\/([\w.]+\/?)\S*/;
      const domain = this.user.domain.value;
      if (domainRegex.test(domain)) {
        this.$confirm('此操作将修改附件域名, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/user/enclosureDomain?domain=' + domain)
              .then(res => {
                if (res.data.code == '1') {
                  this.$message.success('修改成功！');
                  this.getEnclosureDomain();
                } else {
                  this.$message.error(res.data.msg)
                }
              })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入正确域名！')
      }


    },

    //删除自定义域名
    deleteDomain(){
      this.$confirm('此操作将删除附件自定义域名, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/user/enclosureDomain')
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('删除成功！');
                this.getEnclosureDomain();
              } else {
                this.$message.error(res.data.msg)
              }
            }).catch(err => {
          console.log(err)
          this.$message.error('删除失败！')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },

    //获取附件自定义域名
    getEnclosureDomain(){
      axios.get('/user/enclosureDomain')
          .then(res => {
            this.user.domain.value = res.data.data;
          }).catch(err => {
        console.log(err)
        this.$message.error('获取附件自定义域名失败！')
      })
    },

    //修改用户名和密码
    userAccountChange() {
      let username = this.user.account.username;
      let password = this.user.account.password;
      const passwordRegex = /^.{6,30}$/;
      const usernameRegex = /^.{1,30}$/

      if (username == '' || password.length == '') {
        this.$message({
          showClose: true,
          message: '请先输入用户名和密码！',
          type: 'error'
        });
        return
      }

      if (!passwordRegex.test(password)) {
        this.$message({
          showClose: true,
          message: '密码必须6-30位',
          type: 'error'
        });
        return
      }

      if (!usernameRegex.test(username)) {
        this.$message({
          showClose: true,
          message: '用户名必须1-30位',
          type: 'error'
        });
      }

      this.$confirm('此操作将修改用户名和密码, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.post('/user/change', {
          'username': username,
          'password': password
        })
            .then(res => {
              if (res.data.code == '1') {
                this.$message({
                  showClose: true,
                  message: '修改成功！',
                  type: 'success'
                });
                //删除token
                localStorage.removeItem('token');
                this.$router.push('/login')
              } else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: 'error'
                });
              }
            }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '修改错误！',
            type: 'error'
          });

        })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },
    //数据导出
    dataExport() {
      let dataExport = this.user.dataMigration.export;
      if (dataExport.length === 0) {
        this.$message.error('请先选择订阅！');
        return;
      }

      this.$confirm('此操作将选择订阅导出, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let uuids = [];
        for (let i = 0; i < dataExport.length; i++) {
          uuids.push(dataExport[i].uuid);
        }

        axios.get('/user/dataExport?uuids=' + uuids.join(','))
            .then(res => {
              if (res.data.code == '1') {
                // 导出数据
                const blob = new Blob([JSON.stringify(res.data.data)], {type: 'text/plain'});
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', 'dataExport.json');
                document.body.appendChild(link);
                link.click();
                this.$message.success('导出成功！');
              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          console.log(err);
          this.$message.error('导出失败！');
        });
      }).catch(() => {
        this.$message.info('已取消导出');
      });
    },
    //数据导入
    dataImport() {
      this.$refs.fileInput.value = '';
      document.querySelector('.custom-file-input').click();
    },
    //导入到服务器
    dataImportToServer() {
      // 发送到服务器
      axios.post('/user/dataImport', this.user.dataMigration.selectImport)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('导入成功！');
              this.getSubList();
              this.user.dataMigration.dataImportVisible = false;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('导入失败');
      });
    },
    dataExportHandleSelectionChange(val) {
      this.user.dataMigration.export = val;
      console.log(val)
    },
    dataImportHandleSelectionChange(val) {
      this.user.dataMigration.selectImport = val;
      console.log(val)
    },
    //json文件读取
    handleFileSelect(event) {
      const file = event.target.files[0];
      const reader = new FileReader();

      // 解除之前的事件处理函数绑定
      reader.onload = null;

      // 注册新的事件处理函数
      reader.onload = () => {
        try {
          const content = reader.result;
          this.user.dataMigration.import = JSON.parse(content);
          this.user.dataMigration.dataImportVisible = true;
        } catch (err) {
          this.$message.error('请正确上传文件！')
        }
      };
      reader.readAsText(file);
    },
    //获取订阅列表数据
    getSubList() {
      axios.get('/sub/list')
          .then(res => {
            this.user.dataMigration.subData = res.data.data;
          }).catch(err => {
        console.log(err)
        this.$message.error('获取订阅列表数据失败！')
      })
    },
    //上传证书和密钥
    uploadCert() {
      let fileList = this.user.cert.fileList;
      console.log(fileList)
      if (fileList.length < 2) {
        this.$message.error("crt和key都要上传")
        return;
      }
      if (fileList.length > 2) {
        this.$message.error("只能上传crt和key文件");
        return;
      }

      for (let i = 0; i < fileList.length; i++) {
        let sp = fileList[i].name.split('.');
        let ext = sp[sp.length - 1];
        console.log(ext)
        if (!(ext == 'crt' || ext == 'key')) {
          this.$message.error('只能上传crt和key文件')
          return;
        }
        fileList[i].name = ext;
      }

      //构建一个表单把文件传进去
      let param = new FormData()
      for (let index in this.$refs.uploadCertFile.uploadFiles) {
        param.append("files", this.$refs.uploadCertFile.uploadFiles[index].raw)
      }

      axios.post('/user/cert', param, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('上传成功,重启后生效');
              this.getSslList();
            } else {
              this.$message.error(res.data.msg);
            }

          })
          .catch(error => {
            console.error(error);
          });
    },
    certFilehandleChange(file, fileList) {
      this.user.cert.fileList = fileList;
    },
    certFilehandleRemove(file, fileList) {
      this.user.cert.fileList = fileList;
    },
    //获取系统信息
    getSystemInfo() {
      axios.get('/system/info')
          .then(res => {
            if (res.data.code == '1') {
              this.system1.info = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取系统信息失败！');
      })
    },
    //重启项目
    restart() {
      this.$confirm('此操作将重启项目, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.get('/system/restart')
            .then(res => {
              if (res.data.code == '1') {
                this.$message({
                  showClose: true,
                  message: '请等待3-4分钟，正在重启中...',
                  type: 'success'
                });
              } else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: 'error'
                });
              }
            }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '重启失败!',
            type: 'error'
          });

        })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },
    //检查更新
    checkForUpdate() {
      this.system.update.loadingTip = "正在获取更新信息中..."
      this.system.update.windowsVisible = true;
      this.system.update.windowsLoading = true;
      this.getDownloadJarFileStatus();
      axios.get('/system/update/has')
          .then(res => {
            if (res.data.code == 1){
                this.system.update.info = res.data.data;
              this.system.update.windowsLoading = false;
            }else {
              this.system.update.windowsLoading = false;
              this.$message({
                showClose: true,
                message: res.data.msg,
                type: 'error'
              });
            }
          }).catch(err => {
        console.log(err)
        this.system.update.windowsLoading = false;
        this.$message({
          showClose: true,
          message: '无法获取更新信息！',
          type: 'error'
        });
      })
    },

    //获取Jar包文件下载状态
    getDownloadJarFileStatus(){
      axios.get('/system/update/jarStatus?version=' + this.system.update.info.version)
          .then(res => {
            if (res.data.code == 1){
             this.system.update.status = res.data.data;
            }else {
              this.$message({
                showClose: true,
                message: res.data.msg,
                type: 'error'
              });
            }
          }).catch(err => {
        console.log(err)
        this.$message({
          showClose: true,
          message: '无法获取Jar包下载状态！',
          type: 'error'
        });
      })
    },
    //删除最新版本的Jar文件
    deleteDownloadLatestJarFile(){
      this.$confirm('此操作将删除该更新Jar包, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.get('/system/update/delete?version=' + this.system.update.info.version)
            .then(res => {
              if (res.data.code == 1){
                if (res.data.data == true){
                  this.$message({
                    showClose: true,
                    message: "删除成功",
                    type: 'success'
                  });
                  //刷新状态
                  this.getDownloadJarFileStatus();
                }else {
                  this.$message({
                    showClose: true,
                    message: "删除最新版本Jar包失败!",
                    type: 'error'
                  });
                }
              }else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: 'error'
                });
              }
            }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '删除最新版本Jar包失败！',
            type: 'error'
          });
        })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },

    //下载jar包
    downloadJarFile(){
      this.system.update.loadingTip = "正在发送下载请求中，请稍等..."
      this.system.update.windowsLoading = true;
      axios.get('/system/update/download?version=' + this.system.update.info.version)
          .then(res => {
            if (res.data.code == 1){
              this.system.update.windowsLoading = false;
              this.$message({
                showClose: true,
                message: '开始下载Jar包',
                type: 'success'
              });
              //获取下载状态
              let interval = setInterval(() => {
                this.getDownloadJarFileStatus()
                let status = this.system.update.status;
                if (status.download || status.error || status.completed){
                  clearInterval(interval);
                  interval = null;
                  return;
                }
              }, 1000); // 每1秒发送一次请求

            }else {
              this.system.update.windowsLoading = false;
              this.$message({
                showClose: true,
                message: res.data.msg,
                type: 'error'
              });
            }
          }).catch(err => {
        console.log(err)
        this.system.update.loading = false;
        this.$message({
          showClose: true,
          message: '无法下载Jar包！',
          type: 'error'
        });
      })
    },

    //关闭更新窗口前清空数据
    handleCloseUpdateWindow(done){
      console.log(done);
      this.system.update.info.hasUpdate = false;
      this.system.update.info.version = '';
      this.system.update.info.desc = '';
      this.system.update.status.percent = 0.0;
      this.system.update.status.completed = false;
      this.system.update.status.error = false;
      this.system.update.status.download = false;
      this.system.update.status.downloading = true;
      this.system.update.status.version = '';
      this.system.update.windowsVisible = false;
      this.system.update.windowsLoading = false;
      this.system.update.loadingTip = '';
    },

    //取消Jar文件下载
    cancelDownloadJarFile(){
      this.$confirm('此操作将取消下载Jar文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.get('/system/update/cancel')
            .then(res => {
              if (res.data.code == 1){
                this.$message({
                  showClose: true,
                  message: "取消成功！",
                  type: 'success'
                });
                //刷新状态
                this.getDownloadJarFileStatus();
              }else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: 'error'
                });
              }
            }).catch(err => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '取消下载Jar文件失败',
            type: 'error'
          });
        })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },
    //设置ssl
    switchSsl() {
      axios.post('/user/switchSsl?status=' + this.user.cert.switchSsl)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('设置成功，重启后生效！');
            } else {
              this.$message({
                showClose: true,
                message: res.data.msg,
                type: 'error'
              });
            }
          }).catch(err => {
        console.log(err)
        this.$message({
          showClose: true,
          message: '设置ssl失败！',
          type: 'error'
        });

      })
    },
    //获取ssl列表
    getSslList() {
      axios.get('/user/cert')
          .then(res => {
            if (res.data.code == '1') {
              this.user.cert.list = res.data.data;
            } else {
              this.$message({
                showClose: true,
                message: res.data.msg,
                type: 'error'
              });
              return false;
            }
          }).catch(err => {
        console.log(err)
        this.$message({
          showClose: true,
          message: '获取ssl列表!',
          type: 'error'
        });

      })
    },
    //删除ssl
    deleteCert() {
      this.$confirm('此操作将删除证书和密钥, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/user/cert')
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('删除成功！')
                this.getSslList();
              } else {
                this.$message.error(res.data.msg)
              }
            }).catch(err => {
          console.log(err)
          this.$message.error('删除失败！')
        })
      }).catch(() => {
        this.$message({
          showClose: true,
          message: '已取消',
          type: 'info'
        });
      })
    },
    //获取ssl状态
    getSslStatus() {
      axios.get('/user/sslStatus')
          .then(res => {
            if (res.data.code == '1') {
              this.user.cert.switchSsl = res.data.data;
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
        console.log(err)
        this.$message.error('获取ssl状态失败！')
      })
    },
    //获取插件设置
    getPluginSettings(name) {
      this.plugin.settings = []
      this.plugin.settingsVisible = true;
      axios.get('/plugin/settings/' + name)
          .then(res => {
            if (res.data.code == '1') {
              this.plugin.settings = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取插件设置失败！')
      })
    },
    //更新插件设置
    updatePluginSettings() {
      if (this.plugin.settings.length > 0) {
        axios.put('/plugin/settings', this.plugin.settings)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('更新插件设置成功！')
              } else {
                this.$message.error(res.data.mgs);
              }
              this.plugin.settingsVisible = false;
            }).catch(err => {
          console.log(err);
          this.$message.error('更新插件设置错误！');
        });
      } else {
        this.plugin.settingsVisible = false;
      }
    },
    //登出
    logout() {
      //删除token
      localStorage.removeItem('token');
      this.$router.push('/login')
    },
    //删除正在下载的
    deleteDownloading(uuid) {
      this.$confirm('此操作将删除该下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/download/downloading?uuids=' + uuid)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('删除成功！')
              } else {
                this.$message.error(res.data.msg)
              }
            }).catch(err => {
          console.log(err)
          this.$message.error('删除失败！')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    //更新访问路径
    updatePath() {
      const pathRegex = /^[a-zA-Z0-9\-_]{1,30}$/;
      const path = this.user.path;
      if (pathRegex.test(path)) {
        this.$confirm('此操作将修改面板访问路径, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/user/path?path=' + path)
              .then(res => {
                if (res.data.code == '1') {
                  this.$message.success('修改成功！')
                  this.logout();
                } else {
                  this.$message.error(res.data.msg)
                }
              })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的路径！')
      }
    },
    //删除路径
    deletePath() {
      this.$confirm('此操作将删除面板访问路径, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/user/path')
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success("删除面板访问路径成功！");
                this.getPluginList();

              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          this.$message.error("删除面板访问路径错误！")
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取访问路径
    getPath() {
      axios.get('/user/path')
          .then(res => {
            if (res.data.code == '1') {
              this.user.path = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取面板访问路径失败！')
      })
    },
    //更新GithubProxyUrl
    updateGithubProxyUrl() {
      const urlRegex = /^https?:\/\/(?:www\.)?[\w.-]+(?:\.[a-zA-Z]{2,})+(?:\/[\w-./?%&=]*)?$/;
      const githubProxyUrl = this.user.githubProxyUrl;
      if (urlRegex.test(githubProxyUrl)) {
        this.$confirm('此操作将修改Github加速站, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/user/github?githubProxyUrl=' + githubProxyUrl)
              .then(res => {
                if (res.data.code == '1') {
                  this.$message.success('修改成功！')
                  this.getGithubProxyUrl();
                } else {
                  this.$message.error(res.data.msg)
                }
              })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的URL！')
      }
    },
    //删除GithubProxyUrl
    deleteGithubProxyUrl() {
      this.$confirm('此操作将删除Github加速站, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/user/github')
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success("删除Github加速站成功！");
                this.getGithubProxyUrl();

              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          this.$message.error("删除Github加速站错误！")
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取GithubProxyUrl
    getGithubProxyUrl() {
      axios.get('/user/github')
          .then(res => {
            if (res.data.code == '1') {
              this.user.githubProxyUrl = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取Github加速站失败！')
      })
    },

    //更新插件仓库链接
    updatePluginUrl() {
      const urlRegex = /^https?:\/\/(?:www\.)?[\w.-]+(?:\.[a-zA-Z]{2,})+(?:\/[\w-./?%&=]*)?$/;
      const pluginUrl = this.user.pluginUrl;
      if (urlRegex.test(pluginUrl)) {
        this.$confirm('此操作将修改插件仓库链接, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          axios.post('/user/plugin?pluginUrl=' + pluginUrl)
              .then(res => {
                if (res.data.code == '1') {
                  this.$message.success('修改成功！')
                  this.getPluginUrl();
                } else {
                  this.$message.error(res.data.msg)
                }
              })
        }).catch(() => {
          this.$message.info('已取消')
        })
      } else {
        this.$message.error('请输入格式正确的URL！')
      }
    },
    //删除自定义插件仓库链接
    deletePluginUrl() {
      this.$confirm('此操作将删除自定义插件仓库链接使用默认, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/user/plugin')
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success("删除自定义插件仓库链接成功！");
                this.getPluginUrl();
              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          this.$message.error("删除自定义插件仓库链接错误！")
          console.log(err)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取自定义插件仓库链接
    getPluginUrl() {
      axios.get('/user/plugin')
          .then(res => {
            if (res.data.code == '1') {
              this.user.pluginUrl = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取自定义插件仓库链接失败！')
      })
    },
    //格式化日志，避免过长
    formatLog(log) {
      // 将日志按照每120个字符进行分割
      const maxLength = 120;
      let formattedLog = '';
      for (let i = 0; i < log.length; i += maxLength) {
        formattedLog += log.substring(i, i + maxLength) + '\n';
      }
      return formattedLog;
    },
    //已完成下载的选择
    handleSelectionDownloadDone(val) {
      this.download.selectionDownloadDone = val;
    },
    //批量删除已下载
    batchDeleteDownloadDone() {
      this.$confirm('此操作将批量删除下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const uuids = this.download.selectionDownloadDone.map(item => item.uuid);
        axios.delete('/download?uuids=' + uuids)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success('批量删除成功！')
                this.getDownloadDone();
              } else {
                this.$message.error(res.data.msg)
              }
            }).catch(err => {
          console.log(err)
          this.$message.error('批量删除失败！')
        })
      }).catch(() => {
        this.$message.info('已取消批量删除')
      });
    },
    //更新alist配置
    updateAlistInfo() {
      axios.post('/user/alist/update', this.user.submitAlist)
          .then(res => {
            if (res.data.code == '1') {
              this.getAlistInfo()
              return 1;
            } else {
              this.$message.error(res.data.msg)
              return 0;
            }
          })
    },
    //开启alist
    openAlist() {
      this.$confirm('此操作将开启Alist, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.user.submitAlist.open = true;
        axios.post('/user/alist/update', this.user.submitAlist)
            .then(res => {
              if (res.data.code == '1') {
                this.getAlistInfo()
                this.$message.success("alist开启成功")
              } else {
                this.$message.error(res.data.msg)
              }
            })
      }).catch(() => {
        this.$message.info('已取消开启')
      });
    },
    //关闭alist
    closeAlist() {
      this.$confirm('此操作将关闭Alist, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.user.submitAlist.open = false;
        axios.post('/user/alist/update', this.user.submitAlist)
            .then(res => {
              if (res.data.code == '1') {
                this.getAlistInfo()
                this.$message.success("alist关闭成功")
              } else {
                this.$message.error(res.data.msg)
              }
            })
      }).catch(() => {
        this.$message.info('已取消关闭')
      });
    },
    //获取alist配置信息
    getAlistInfo() {
      axios.get('/user/alist/info')
          .then(res => {
            if (res.data.code == '1') {
              this.user.alistInfo = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取alist配置信息失败！')
      })
    },
    //查看最近历史日志
    getHistoryLogs() {
      axios.get('/system/logs/history/latest?' + 'minutes=' + this.system.historyLogsLatest + '&level=' + this.system.historyLogsLevel)
          .then(res => {
            if (res.data.code == '1') {
              this.system.historyLogs = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('最近日志失败！')
      })
    },
    getHistoryLogsByTime() {
      axios.get('/system/logs/history/between?start=' + this.system.selectHistoryByTime[0] + '&end=' + this.system.selectHistoryByTime[1] + '&level=' + this.system.historyLogsLevel)
          .then(res => {
            if (res.data.code == '1') {
              this.system.historyLogs = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取日志失败！')
      })
    },
    //下载日志
    downloadLogs() {
      //不能为空
      if (this.system.historyLogs.length != 0) {
        const blob = new Blob([this.system.historyLogs.join("\n")], {type: 'text/plain'});
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', this.system.historyLogsLevel + '.log');
        document.body.appendChild(link);
        link.click();
      } else {
        this.$message.error('不能下载空日志！');
      }
    },
    removeBlackIp(item) {
      let index = this.user.ipAddressFilter.blacklist.indexOf(item)
      if (index !== -1) {
        this.user.ipAddressFilter.blacklist.splice(index, 1)
      }
    },
    addBlacklist() {
      this.user.ipAddressFilter.blacklist.push('');
    },
    removeWhiteIp(item) {
      let index = this.user.ipAddressFilter.whitelist.indexOf(item)
      if (index !== -1) {
        this.user.ipAddressFilter.whitelist.splice(index, 1)
      }
    },
    addWhitelist() {
      this.user.ipAddressFilter.whitelist.push('');
    },
    //清空黑白名单
    clearWhiteBlacklist() {
      this.user.ipAddressFilter.blacklist = []
      this.user.ipAddressFilter.whitelist = []
    },
    //提交黑白名单
    submitWhiteBlacklist() {
      this.$confirm('此操作将提交黑白名单, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.post('/user/addressFilter/update',this.user.ipAddressFilter)
            .then(res => {
              if (res.data.code == '1') {
                this.$message.success("提交黑白名单成功！")
                this.getAddressFilter();
              } else {
                this.$message.error(res.data.msg);
              }
            }).catch(err => {
          console.log(err);
          this.$message.error(err.toString());
        })
      }).catch(() => {
        this.$message.info('已取消提交')
      });
    },
    //获取黑白名单
    getAddressFilter() {
      axios.get('/user/addressFilter')
          .then(res => {
            if (res.data.code == '1') {
              this.user.ipAddressFilter = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取黑白名单错误！')
      })

    },
    //获取apiToken数据
    getApiTokenInfo(){
      axios.get('/user/apiTokenInfo')
          .then(res => {
            if (res.data.code == '1') {
              this.user.apiToken = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取apiToken信息失败！')
      })
    },
    //创建apiToken
    createApiToken(){
      axios.get('/user/createApiToken')
          .then(res => {
            if (res.data.code == '1') {
              this.user.apiToken.apiToken = res.data.data;
              this.user.apiToken.hasApiToken = true;
              this.$message.success("创建apiToken成功！")
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('创建apiToken失败！')
      })
    },
    //移除apiToken
    removeApiToken(){
      axios.delete('/user/apiToken')
          .then(res => {
            if (res.data.code == '1') {
              this.user.apiToken.apiToken = '';
              this.user.apiToken.hasApiToken = false;
              this.$message.success("移除apiToken成功！")
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('移除apiToken失败！')
      })
    },
    //获取botInfo
    getBotInfo(){
      axios.get('/user/botInfo')
          .then(res => {
            if (res.data.code == '1') {
              this.user.botInfo = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('获取botInfo失败！')
      })
    },
    //更新botInfo
    updateBotInfo(){
      if (this.user.botInfo.isOpen){
        if (this.user.botInfo.username == ''
            || this.user.botInfo.token == ''
            || this.user.botInfo.username == null
            || this.user.botInfo.token == null){
          this.$message.error("username和token不能为空");
          return;
        }
      }
      axios.put('/user/botInfo', this.user.botInfo)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('更新botInfo成功！重启后失效');
              this.getBotInfo();
            } else {
              this.$message.error(res.data.mgs);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error('更新botInfo错误！');
      });
    },
    //更新api文档状态
    updateApiDocStatus(){
      axios.post('/user/apiDocStatus',this.user.apiDoc)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success("修改成功！重启后失效")
              this.getAddressFilter();
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error(err.toString());
      })
    },
    //获取api文档状态
    getApiDocStatus(){
      axios.get('/user/apiDocStatus')
          .then(res => {
            if (res.data.code == '1') {
              this.user.apiDoc.status = res.data.data;
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
        console.log(err);
        this.$message.error(err.toString())
      })
    }

  }
}
</script>

<style scoped>
.footer {
  position: fixed;
  bottom: 20px;
  /* 上提的距离 */
  left: 0;
  width: 100%;
  text-align: center;
}

.Download {
  height: 90vh;
  overflow-y: scroll;
}

#app {
  display: flex;
  /*height: 100vh;*/
}

.sidebar {
  width: 200px;
  background-color: #FFFFFF;
}

.main {
  flex: 1;
  padding: 20px;
}

#logspage {
  display: flex;
  flex-direction: column;
}

.logs {
  height: 70vh;
  /* 将高度设置为页面高度的 80% */
  padding: 20px;
  overflow-y: scroll;
  background-color: #f0f0f0;
}

.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #f0f9eb;
}

/*去除file按钮的样式*/
.custom-file-input {
  opacity: 0;
  width: 0;
  height: 0;
}

.pluginList {
  height: 30vh;
  overflow-y: scroll;
}

/*排序按钮*/
.reverse-button {
  position: fixed;
  bottom: 2%;
  right: 2%;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.up-button {
  position: fixed;
  bottom: 7%;
  right: 2%;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  margin-bottom: 1.5em;
}

.footer {
  position: fixed;
  bottom: 20px;
  /* 上提的距离 */
  left: 0;
  width: 100%;
  text-align: center;
}

.logs {
  height: 70vh;
  /* 将高度设置为页面高度的 80% */
  padding: 20px;
  overflow-y: scroll;
  background-color: #f0f0f0;
}

/*去除file按钮的样式*/
.custom-file-input {
  opacity: 0;
  width: 0;
  height: 0;
}
</style>
<!-- 全局样式 -->
<style>
/*检查更新弹窗*/
.custom-alert-box .el-message-box__content {
  max-height: 400px; /* 设置最大高度 */
  overflow-y: auto;  /* 启用垂直滚动条 */
}
</style>