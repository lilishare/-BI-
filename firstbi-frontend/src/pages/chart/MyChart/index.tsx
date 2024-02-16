// @ts-ignore
import React, {useEffect, useState} from "react";
import {listMyChartVoByPageUsingPost} from "@/services/firstbi/chartController";
// @ts-ignore
import {Avatar, Card, List, message, Result} from "antd";
// @ts-ignore
import Search from "antd/es/input/Search";
import ReactEcharts from "echarts-for-react"
import {useModel} from "@umijs/max";
// @ts-ignore
import {PaginationAlign, PaginationPosition} from "antd/es/pagination/Pagination";
import {useInterval} from "ahooks";


const MyChart: React.FC = () => {
  // 定义初始参数
  const initSearchParams = {
    current: 1,
    pageSize: 6,
    sortField: 'createTime',
    sortOrder: 'desc',
  }
  const {initialState} = useModel('@@initialState');
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({...initSearchParams});
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const {currentUser} = initialState ?? {};
  const [position] = useState<PaginationPosition>('bottom');
  const [align] = useState<PaginationAlign>('center');

  // 获取图表信息
  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listMyChartVoByPageUsingPost(searchParams)
      if (res.data) {
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0)
        // 隐藏图表的标题
        if (res.data.records) {
          res.data.records.forEach(item => {
            const chartOption = JSON.parse(item.genChart ?? '{}');
            if (chartOption.title) {
              chartOption.title = undefined;
              item.genChart = JSON.stringify(chartOption)
            }
          })
        }
        setLoading(false);
      } else {
        message.error("获取我的图表失败")
      }
    } catch (e) {
      message.error("获取图表失败" + e)
    }
  }
  useEffect(() => {
    loadData();
  }, [searchParams]);
  //每30s加载一次数据
  useInterval(() => {
    loadData();
  }, 30000);

  return (
    <div className="my-chart">
      <div>
        <Search placeholder="请输入图表名称" enterButton loading={loading} onSearch={(value) => {
          // 设置搜索条件
          setSearchParams({
            ...initSearchParams,
            chartName: value,
          })
        }}/>
      </div>
      <div className="margin-t-16"/>
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
        }}
        pagination={{
          position, align,
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            })
          },
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total,
        }}
        loading={loading}
        dataSource={chartList}
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card style={{width: '100%'}}>
              <List.Item.Meta
                avatar={<Avatar src={currentUser && currentUser.userAvatar}/>}
                title={item.chartName}
                description={item.chartType ? '图表类型：' + item.chartType : undefined}
              />
              {
                item.execStatus === 0 && <>
                  <Result
                    status="warning"
                    title="待生成"
                    subTitle={item.execMessage ?? '当前图表生成队列繁忙，请耐心等候'}
                  />
                </>
              }
              {
                item.execStatus === 1 && <>
                  <Result
                    status="info"
                    title="图表生成中"
                    subTitle={item.execMessage}
                  />
                </>
              }
              {
                item.execStatus === 2 && <>
                  <div style={{marginBottom: 16}}/>
                  <p>{'分析目标：' + item.goal}</p>
                  <div style={{marginBottom: 16}}/>
                  <ReactEcharts option={item.genChart && JSON.parse(item.genChart)}/>
                </>
              }
              {
                item.execStatus === 3 && <>
                  <Result
                    status="error"
                    title="图表生成失败"
                    subTitle={item.execMessage}
                  />
                </>
              }
            </Card>
          </List.Item>
        )}

      />
    </div>
  )
};
export default MyChart;
