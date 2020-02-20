module.exports = {
    devServer: {
        // 设置代理
        proxy: {
            "/ns": {
                target: "http://localhost:6767/",
                ws: true,
                changOrigin: true,
                pathRequiresRewrite: {
                    "^/ns": "/"
                }
            }
        }
    }
};
