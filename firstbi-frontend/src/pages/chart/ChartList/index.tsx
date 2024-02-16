// @ts-ignore
import React, {useEffect, useRef, useState} from "react";
import {
  deleteChartUsingPost,
  listMyChartVoByPageUsingPost,
  reloadChartUsingGet
} from "@/services/firstbi/chartController";
import { CHART_TYPE } from '@/constants/chart/chartType';
import { CHART_STATUS } from '@/constants/chart/chartStatus';
// @ts-ignore
import {
  Button,
  Collapse,
  Dropdown,
  MenuProps,
  message,
  Modal,
  Result,
  Space,
  Tag,
  Typography
} from 'antd';

// @ts-ignore
import ReactEcharts from "echarts-for-react"
import {useModel} from "@umijs/max";
// @ts-ignore
import {useInterval} from "ahooks";
import {
  DeleteOutlined,
  EllipsisOutlined,
  ExclamationCircleFilled,
  EyeOutlined
} from '@ant-design/icons';
import ProList from '@ant-design/pro-list';
import { ProListMetas } from '@ant-design/pro-list/lib';
import { ActionType } from '@ant-design/pro-table';
import UpdateChartModal from "@/pages/chart/ChartList/components/UpdateChartModal";



const MyChart: React.FC = () => {
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ ...initSearchParams });
  const [loading, setLoading] = useState<boolean>(false);
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const actionRef = useRef<ActionType>();
  const [disableOption, setDisableOption] = useState<boolean>(false);
  const [updateData, setUpdateData] = useState<API.Chart>({});
  const [chartId, setChartId] = useState<number>(0);
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);

  //默认搜索参数
  const initSearchParams = {
    chartName: '',
    chartType: '',
    current: 1,
    pageSize: 2,
    sortField: 'createTime',
    sortOrder: 'desc',
  };
  /**
   * 加载数据
   */
  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listMyChartVoByPageUsingPost(searchParams);
      const data = res?.data;
      if (res.code === 0 && data) {
        // message.success('加载成功');
        // 隐藏图表的标题
        if (data.records) {
          data.records.forEach(item => {
            const chartOption = JSON.parse(item.genChart ?? '{}');
            if (chartOption.title) {
              chartOption.title = undefined;
              item.genChart = JSON.stringify(chartOption)
            }
          })
        }
        setChartList(data?.records ?? []);
        setTotal(data.total ?? 0);
      } else {
        message.error('获取图表列表失败');
      }
    } catch (e: any) {
      message.error('获取图表列表失败', e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  //每30s加载一次数据
  useInterval(() => {
    loadData();
  }, 30000);
  /**
   * 删除图表数据
   * @param params
   */
  const handleDelete = async (params: number) => {
    if (params <= 0 || params === undefined) {
      message.error('图表id为空');
      return;
    }
    const data = {
      id: params,
    };
    try {
      const res = await deleteChartUsingPost(data);
      if (res.code === 0 && data) {
        message.success('删除成功');
        setChartId(0);
        //todo 列表刷新失效
        if (actionRef.current) {
          actionRef.current.reload();
        }
      } else {
        message.error('删除失败');
      }
    } catch (e: any) {
      message.error('删除失败', e.message);
    }
  };
  /**
   * 操作 下拉菜单：查看原始数据、删除
   */
  const items: MenuProps['items'] = [
    {
      key: '1',
      label: (
        <Space
          onClick={() => {
            setUpdateModalVisible(true);
          }}
        >
          <EyeOutlined />
          <Typography.Link>查看</Typography.Link>
        </Space>
      ),
      disabled: disableOption,
    },
    {
      key: '2',
      danger: true,
      onClick: () => {
        Modal.confirm({
          title: '删除对话',
          icon: <ExclamationCircleFilled />,
          okType: 'danger',
          content: '确认要删除该图表吗？',
          okText: '确认',
          cancelText: '取消',
          onOk() {
            handleDelete(chartId);
          },
        });
      },
      label: (
        <Space>
          <DeleteOutlined />
          <div>删除</div>
        </Space>
      ),
    },
  ];

  /**
   * 处理图表类型
   */
  const handleType = () => {
    return CHART_TYPE.reduce((obj: any, item) => {
      obj[item.value] = item.label;
      return obj;
    }, {});
  };
  /**
   * 手动重新生成图表
   * @param params
   */
  const reloadChart = async (params: number) => {
    if (params === undefined) {
      message.error('id为空');
      return;
    }
    const data = {
      chartId: params,
    };
    try {
      console.log(data)
      const res = await reloadChartUsingGet(data);
      if (res.code === 0 && data) {
        message.success('请求成功，图表生成中请稍后');
      } else {
        message.error('重新生成图表失败');
      }
    } catch (e: any) {
      message.error('重新生成图表失败', e.message);
    }
  };


  /**
   * 列表元素
   */
  const metas: ProListMetas<API.Chart> = {
    id: {
      title: 'id',
      dataIndex: 'id',
      valueType: 'index',
      search: false,
    },
    title: {
      title: '图表名称',
      dataIndex: 'chartName',
    },
    subTitle: {
      title: '图表类型',
      dataIndex: 'chartType',
      valueEnum: handleType(),
      render: (_, item) => {
        return <Tag color="#5BD8A6">{'图表类型：' + item.chartType}</Tag>;
      },
    },
    content: {
      search: false,
      render: (_, item) => {
        return (
          <div style={{ flex: 1 }}>
            <div>
              <p>{item.goal}</p>
            </div>
            {item.execStatus === 0 && (
              <div>
                <Result status="warning" title="等待处理" subTitle={item.execMessage} />
              </div>
            )}
            {item.execStatus === 1 && (
              <div>
                <Result status="info" title="图表生成中" subTitle={item.execMessage} />
              </div>
            )}
            {item.execStatus === 3 && (
              <div>
                <Result
                  status="error"
                  title="图表生失败"
                  subTitle={item.execMessage}
                  extra={[
                    <Button
                      key="tryAgain"
                      type="primary"
                      danger
                      onClick={() => {
                        reloadChart(item.id as number);
                      }}
                    >
                      请重试
                    </Button>,
                  ]}
                />
              </div>
            )}
            {item.execStatus === 2 && (
              <div style={{ width: '100%' }}>
                <ReactEcharts option={JSON.parse(item.genChart ?? '{}')} />
              </div>
            )}
            <div>
              <Collapse
                bordered={false}
                items={[
                  {
                    key: item.id,
                    label: 'Ai分析结论',
                    children: <p>{item.genResult}</p>,
                  },
                ]}
              />
            </div>
          </div>
        );
      },
    },
    actions: {
      cardActionProps: 'extra',
      render: (text, row) => {
        return (
          <Dropdown menu={{ items }} trigger={['click']} placement="bottom" arrow>
            <a
              onClick={(e) => {
                e.preventDefault();
                setUpdateData(row);
                setChartId(row.id ?? 0);
                setDisableOption(CHART_STATUS['2'].value !== row.execStatus);
              }}
            >
              <EllipsisOutlined style={{ fontSize: 20 }} />
            </a>
          </Dropdown>
        );
      },
    },
  };

  return (
    <div className="my-chart-page">
      <ProList<API.Chart>
        grid={{ gutter: 16, column: 2 }}
        ghost={true}
        loading={loading}
        actionRef={actionRef}
        search={{
          labelWidth: 'auto',
          defaultCollapsed: false,
        }}
        request={async (params) => {
          setSearchParams({
            ...searchParams,
            chartName: params.chartName,
            chartType: params.chartType,
          });
        }}
        rowKey="id"
        pagination={{
          onChange: (page, pageSize) => {
            // console.log(page, pageSize);
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            });
          },
          current: searchParams?.current,
          pageSize: searchParams?.pageSize,
          total: total,
        }}
        dataSource={chartList}
        metas={metas}
      />
      <UpdateChartModal
        modalVisible={updateModalVisible}
        oldData={updateData}
        // columns={columns}
        onSubmit={() => setUpdateModalVisible(false)}
        onCancel={() => setUpdateModalVisible(false)}
      />


    </div>
  );



};
export default MyChart;
