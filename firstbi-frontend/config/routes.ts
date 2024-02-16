export default [
  {
    path: '/user',
    layout: false,
    routes: [{ name: '登录', path: '/user/login', component: './User/Login' }],
  },
  {
    layout: false,
    routes: [{ name: '注册', path: '/user/register', component: './User/Register' }],
  },
  {
    path: '/chart',
    icon: 'barChart',
    name: '图表',
    routes: [
      {
        path: '/chart/add_chart',
        name: '智能分析(同步)',
        icon: 'barChart',
        component: './chart/AddChart',
      },
      {
        path: '/chart/add_chart_async',
        name: '智能分析（异步线程池）',
        icon: 'barChart',
        component: './chart/AddChartAsync',
      },
      {
        path: '/chart/add_chart_mq',
        name: '智能分析（消息队列）',
        icon: 'barChart',
        component: './chart/AddChartMq',
      },
      // {
      //   path: '/chart/my_chart',
      //   name: '我的图表',
      //   icon: 'pieChart',
      //   component: './chart/MyChart',
      // },
      {
        path: '/chart/chart_list',
        name: '我的图表',
        icon: 'pieChart',
        component: './chart/ChartList' ,
      },
    ],
  },
  {
    path: '/admin',
    name: '管理页',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      { path: '/admin', redirect: '/admin/sub-page' },
      { path: '/admin/sub-page', name: '二级管理页', component: './Admin' },
    ],
  },
  { path: '/', redirect: '/chart/add_chart' },
  { path: '*', layout: false, component: './404' },
];
