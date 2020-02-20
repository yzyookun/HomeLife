import Vue from 'vue'
import axios from 'axios'

export default {
  vue: undefined,
  install (vue, options) {
    Vue.prototype.$api = this
    vue.mixin({
      created () {
        if (!(Vue.prototype.$api.vue)) {
          Vue.prototype.$api.vue = this
        }
      }
    })
  },
  httpPost (url, params) {
    return axios({
      method: 'post',
      url,
      data: JSON.stringify(params),
      timeout: this.timeout,
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Content-Type': 'application/json; charset=UTF-8'
      }
    }).then(response => response.data)
  },
  status (clientId) {
    return this.httpPost('/ns/admin/status/' + clientId, {})
  },
  setting (clientId, params) {
    return this.httpPost('/ns/admin/setting/' + clientId, params)
  }
}
