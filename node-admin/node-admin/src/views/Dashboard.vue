<template>
  <div>
    <x-header title="智能家电管理终端" :left-options="{showBack:false}"></x-header>

    <divider>家电遥控</divider>

    <!-- 温湿度测量器 -->
    <card class="eleCard" :header="{title: dht.name}" v-for="dht in iotModel.dhts" :key="dht.uuid">
      <div class="eleCardContent" slot="content">
        <flexbox>
          <flexbox-item>
            <div class="centerText">
              <span class="dhtCellValue">
                {{dht.temperature}}
              </span>
              <span class="dhtCellUnit">
                 ℃
               </span>
            </div>
          </flexbox-item>
          <flexbox-item class="leftBorder">
            <div class="centerText">
              <span class="dhtCellValue">
                {{dht.humidity}}
              </span>
              <span class="dhtCellUnit">
                 ％
               </span>
            </div>
          </flexbox-item>
        </flexbox>
      </div>
    </card>

    <!-- 灯光 -->
    <card class="eleCard" :header="{title: '灯光'}">
      <div class="eleCardContent" slot="content">
        <flexbox :gutter="0" warp="warp">
          <flexbox-item v-for="(light,index) in iotModel.lights" :key="index">
            <div :class="(index%2)?'leftBorder':''" @click="switchLight(light)">
              <div class="tTitle">
                &nbsp;&nbsp;{{light.name}}
              </div>
              <div class="centerText">
                <i class="material-icons text-icon-button">{{light.power?'wb_incandescent':'panorama_fish_eye'}}</i>
              </div>
              <div class="lightPowerDescription">
                {{light.power?'on':'off'}}
              </div>
            </div>
          </flexbox-item>
        </flexbox>
      </div>
    </card>

    <!-- 风扇 -->
    <div @click="showFanDialog(copy(fan))" v-for="fan in iotModel.fans" :key="fan.uuid">
      <card class="eleCard" :header="{title: fan.name}">
        <div class="eleCardContent" slot="content">
          <flexbox>
            <flexbox-item :span="4">
              <div class="centerText">
                <i :class="'material-icons text-icon-button'+(fan.power?(' fl'+fan.level):'')">toys</i>
              </div>
            </flexbox-item>
            <flexbox-item class="leftBorder desCell">
              电源：{{fan.power?'开':'关'}} <br/>
              风速：LV{{fan.level}} <br/>
            </flexbox-item>
          </flexbox>
        </div>
      </card>
    </div>

    <!-- 空调 -->
    <div @click="showAcDialog(copy(ac))" v-for="ac in iotModel.acs">
      <card class="eleCard" :header="{title: ac.name}">
        <div class="eleCardContent" slot="content">
          <flexbox>
            <flexbox-item :span="4">
              <div class="centerText">
                <i class="material-icons text-icon-button">waves</i>
              </div>
            </flexbox-item>
            <flexbox-item class="leftBorder desCell">
              电源：{{ac.power?'开':'关'}} <br/>
              温度：{{ac.target}}℃ <br/>
            </flexbox-item>
          </flexbox>
        </div>
      </card>
    </div>

    <!-- 远程开机 -->
    <card class="eleCard" :header="{title: '远程开机'}">
      <div class="eleCardContent" slot="content">
        <flexbox v-for="pcs in iotModel.pcs">
          <flexbox-item v-for="pc in pcs">
            <div @click="wol(copy(pc))">
              <div class="centerText">
                <i class="material-icons text-icon-button">desktop_mac</i>
              </div>
              <div class="centerText" style="font-size: 0.8rem; color: #353535">
                {{pc.name}}
              </div>
            </div>
          </flexbox-item>
        </flexbox>
      </div>
    </card>

    <divider>远端状态</divider>

    <card style="border-radius: .3rem" :header="{title: '内存'}">
      <div slot="content">
        <v-chart class="vChart" :data="iotModel.chip.rams" prevent-default :height="180">
          <v-scale x/>
          <v-scale y :min="0" :max="100" alias="平均内存" :tick-count="5"/>
          <v-tooltip :show-item-marker="false" show-x-value/>
          <v-point :style="{ stroke: '#fff',
          lineWidth: 1
        }" shape="smooth"/>
          <v-line shape="smooth"/>
        </v-chart>
      </div>
    </card>


    <divider>已经到底了</divider>

    <div>
      <!-- 风扇启动弹窗 -->
      <div v-transfer-dom>
        <x-dialog :show.sync="fanDialog.show" :hide-on-blur="true">
          <group title="风扇">
            <cell title="电源" primary="content" inline-desc="">
              <x-switch v-model="fanDialog.setting.power" title=" "></x-switch>
            </cell>
            <cell title="风速" primary="content" inline-desc="">
              <rater v-model="fanDialog.setting.level" :step="1" :min="1" :max="4">
              </rater>
            </cell>
            <cell>
              <br/>
              <flexbox>
                <flexbox-item>
                  <x-button @click.native="fanDialogCommit" :gradients="['#FF9500','#FF5E3A']">确认</x-button>
                </flexbox-item>
              </flexbox>
            </cell>
          </group>
        </x-dialog>
        <!-- 空调启动弹窗 -->
      </div>
      <div v-transfer-dom>
        <x-dialog :show.sync="acDialog.show" :hide-on-blur="true">
          <group title="空调">
            <cell title="电源" primary="content" inline-desc="">
              <x-switch v-model="acDialog.setting.power" title=" "></x-switch>
            </cell>
            <cell title="温度" primary="content" inline-desc="">
              <picker :data='years' v-model='acDialog.setting.temp' @on-change='change'></picker>
            </cell>
            <cell>
              <br/>
              <flexbox>
                <flexbox-item>
                  <x-button @click.native="acDialogCommit" :gradients="['#FF9500','#FF5E3A']">确认</x-button>
                </flexbox-item>
              </flexbox>
            </cell>
          </group>
        </x-dialog>
      </div>

    </div>
  </div>
</template>

<script>
  import {
    XHeader, Divider, Card,
    Flexbox, FlexboxItem, Grid, GridItem,
    Group, XSwitch, Cell, Range, Rater,
    XButton, Alert, Picker,
    XDialog, TransferDomDirective as TransferDom,
    VChart, VLine, VPoint, VScale, VTooltip
  } from 'vux'
  let years = []
  for (var i = 16; i <= 30; i++) {
    years.push({
      name: i + '°C',
      value: i + ''
    })
  }
  export default {
    name: 'Dashboard',
    components: {
      XHeader,
      Divider,
      Card,
      Flexbox,
      FlexboxItem,
      Grid,
      Picker,
      GridItem,
      Group,
      XSwitch,
      Cell,
      Range,
      Rater,
      XButton,
      Alert,
      XDialog,
      VChart,
      VLine,
      VPoint,
      VScale,
      VTooltip
    },
    directives: {
      TransferDom
    },
    data () {
      return {
        years: [years],
        iotModel: {
          node: true,
          dhts: [
            {
              uuid: 'dht',
              name: 'DHT22',
              power: true,
              temperature: '00.00',  // string hack init number
              humidity: '00.00'     // string hack init number
            }
          ],
          lights: [
            {
              uuid: 'lights1',
              name: '卧室',
              power: false
            },
            {
              uuid: 'lights2',
              name: '客厅',
              power: false
            }
          ],
          fans: [
            {
              uuid: 'fan1',
              name: '风扇',
              power: false,
              level: 1
            }
          ],
          acs: [
            {
              uuid: 'ac1',
              name: '空调',
              power: false,
              target: 26
            }
          ],
          pcs: [
            {
              uuid: 'pc1',
              name: '书房',
              power: false,
              mac: 'aa'
            }
          ],
          chip: {
            rams: [
              {date: '-7', value: 0},
              {date: '-6', value: 0},
              {date: '-5', value: 0},
              {date: '-4', value: 0},
              {date: '-3', value: 0},
              {date: '-2', value: 0},
              {date: '-1', value: 0},
              {date: '现在', value: 0}
            ]
          }
        },
        fanDialog: { // 风扇
          show: false,
          setting: {
            model: 'FAN',
            uuid: '',
            power: false,
            level: 0
          }
        },
        acDialog: {  // 空调
          show: false,
          setting: {
            model: 'ac',
            uuid: '',
            power: false,
            temp: ['26']
          }
        },
        timer: undefined
      }
    },
    methods: {
      change (value) {
        console.log('new Value', value)
      },
      none () {
      },
      nodeResponse (node) {
        if (node && node.node) {
          for (let i = 0; i < node.chip.rams.length; i++) {
            node.chip.rams[i] = {
              date: (i === node.chip.rams.length - 1) ? '现在' : ('-' + (node.chip.rams.length - 1 - i) + 's'),
              value: node.chip.rams[i]
            }
          }
          // 将电脑分成两个两个一组
          let pcs = []
          let tmp = []
          for (let i = 0; i < node.pcs.length; i++) {
            tmp.push(node.pcs[i])
            if (tmp.length >= 3) {
              pcs.push(tmp)
              tmp = []
            }
          }
          if (tmp.length > 0) pcs.push(tmp)
          node.pcs = pcs
          // 赋值
          this.iotModel = node
          return true
        }
      },
      copy (obj) {
        return JSON.parse(JSON.stringify(obj))
      },
      switchLight (light) { // 灯光控制
        let turn = !(light.power)
        if (confirm('您确定' + (turn ? '开启' : '关闭') + light.name + '吗？')) {
          this.$api.setting('node-client-test', {
            model: 'LIGHT',
            uuid: light.uuid,
            power: turn
          }).then(this.nodeResponse)
        }
      },
      showFanDialog (fan) { // 风扇控制
        this.fanDialog.setting.uuid = fan.uuid
        this.fanDialog.setting.power = fan.power
        this.fanDialog.setting.level = fan.level
        this.fanDialog.show = true
      },
      fanDialogCommit () { // 风扇启动弹窗控制
        let _this = this
        this.$api.setting('node-client-test', {
          model: this.fanDialog.setting.model,
          uuid: this.fanDialog.setting.uuid,
          power: this.fanDialog.setting.power,
          value: this.fanDialog.setting.level
        }).then(response => {
          if (_this.nodeResponse(response)) {
            _this.fanDialog.show = false
          }
        })
      },
      acDialogCommit () { // 空调启动弹窗控制
        let _this = this
        this.$api.setting('node-client-test', {
          model: this.fanDialog.setting.model,
          uuid: this.fanDialog.setting.uuid,
          power: this.fanDialog.setting.power,
          value: this.fanDialog.setting.level
        }).then(response => {
          if (_this.nodeResponse(response)) {
            _this.fanDialog.show = false
          }
        })
      },
      showAcDialog (ac) { // 空调控制
        this.acDialog.setting.uuid = ac.uuid
        this.acDialog.setting.power = ac.power
        this.acDialog.setting.level = ac.level
        this.acDialog.show = true
      },
      wol (pc) {
        let _this = this
        if (confirm('您确定唤醒' + pc.name + '的电脑吗？')) {
          this.$api.setting('node-client-test', {
            model: 'WOL',
            uuid: pc.uuid
          }).then(response => {
            _this.nodeResponse(response)
            alert('您选择电脑已经唤醒')
          })
        }
      },
      iTimer () {
        this.$api.status('node-client-test').then(this.nodeResponse)
        setTimeout(this.iTimer, 5000)
      }
    },
    mounted () {
      this.iTimer()
    },
    destroyed () {
      clearTimeout(this.timer)
    }
  }
</script>

<style scoped>

  /* 卡片 */

  .eleCard {
    margin: .4rem .6rem .4rem .6rem;
    border-radius: .3rem;
  }

  .eleCardContent {
    padding: .4rem;
  }

  /* 通用 */

  .centerText {
    text-align: center;
  }

  .leftBorder {
    border-left: #0000000c 3px solid;
  }

  .text-icon-button {
    font-size: 3rem;
  }

  .desCell {
    padding-left: 1.5rem;
    font-size: 0.8rem;
  }

  /* 温湿度 */

  .dhtCellValue {
    color: green;
    font-weight: bold;
    font-size: 2.5rem;
  }

  .dhtCellUnit {
    color: gray;
    font-size: 1.5rem;
  }

  /* 灯光 */

  .lightPowerDescription {
    text-align: center;
    font-size: .7rem;
    color: #00000077;
  }

  /* 风扇 */

  .fl1 {
    animation: rotate360 4s linear infinite;
  }

  .fl2 {
    animation: rotate360 3s linear infinite;
  }

  .fl3 {
    animation: rotate360 2s linear infinite;
  }

  .fl4 {
    animation: rotate360 1s linear infinite;
  }

  @keyframes rotate360 {
    from {
      transform: rotate(360deg);
    }
    to {
      transform: rotate(0deg);
    }
  }


</style>
