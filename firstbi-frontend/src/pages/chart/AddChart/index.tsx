// @ts-ignore
import React, {useState} from "react";
// @ts-ignore
import {Button, Card, Col, Divider, Form, Input, message, Row, Select, SelectProps, Space, Spin, Upload} from "antd";
import {UploadOutlined} from "@ant-design/icons";
import {getChartUsingPost} from "@/services/firstbi/chartController";
import ReactEcharts from "echarts-for-react"
// @ts-ignore
import { Typography } from 'antd';

const formItemLayout = {
  labelCol: {span: 4},
  wrapperCol: {span: 8},
};
const { Paragraph } = Typography;
const AddChart: React.FC = () => {
  const options: SelectProps['options'] = [];
// eslint-disable-next-line react-hooks/rules-of-hooks
  const [chart, setChart] = useState<API.BiResponse>();
  const [option, setOption] = useState<any>();
  const [submitting, setSubmitting] = useState<boolean>(false);

  const handleChange = (value: string) => {
    console.log(`selected ${value}`);
  };

  options.push({
    value: "柱形图",
    label: "柱形图",
  });
  options.push({
    value: "折线图",
    label: "折线图",
  });
  options.push({
    value: "饼图",
    label: "饼图",
  });
  options.push({
    value: "面积图",
    label: "面积图",
  });
  options.push({
    value: "雷达图",
    label: "雷达图",
  });
  const onFinish = async (values: any) => {
    // 避免重复提交
    if (submitting) {
      return;
    }
    setSubmitting(true);
    setChart(undefined);
    setOption(undefined);
    // 对接后端，上传数据
    let chartTypeStr = "";

    // eslint-disable-next-line eqeqeq
    if (values.chartType != null) {
      values.chartType.forEach((tag: string) => {
        chartTypeStr += tag;
        chartTypeStr += " ";
      })
    }

    const params = {
      ...values,
      chartType: chartTypeStr,
      file: undefined,
    };

    try {
      const res = await getChartUsingPost(params, {}, values.file.file.originFileObj);
      if (!res?.data) {
        message.error('分析失败');
      } else {
        console.log('分析成功');
        const chartOption = JSON.parse(res.data.genChart ?? '');
        console.log("-------chartOption--------")
        console.log(chartOption)
        console.log(typeof chartOption)
        if (!chartOption) {
          throw new Error('图表代码解析错误')
        } else {
          setChart(res.data);
          setOption(chartOption);
        }
      }
    } catch (e: any) {
      message.error('分析失败，' + e.message);
    }
    setSubmitting(false);

  }

  return (
    <div className="add-chart">
      <Row gutter={24}>
        <Col span={12}>
          <Card title="智能分析">

            <Form name="addChart" onFinish={onFinish} style={{maxWidth: 600}} labelAlign="left" labelCol={{span: 4}}
                  wrapperCol={{span: 16}}
            >
              <Form.Item
                label="分析目标"
                name="goal"
                rules={[{required: true, message: 'Please input!'}]}
              >
                <Input.TextArea placeholder="请输入分析目标"/>
              </Form.Item>
              <Form.Item
                {...formItemLayout}
                name="chartName"
                label="图表名称"
                // rules={[{ required: true, message: '请输入图表名称' }]}
              >
                <Input placeholder="请输入图表名称"/>
              </Form.Item>
              <Form.Item
                label="图标类型"
                name="chartType"
              >
                <Select
                  mode="tags"
                  style={{width: '100%'}}
                  placeholder="例如：柱形图"
                  onChange={handleChange}
                  options={options}
                />
              </Form.Item>

              <Form.Item
                name="file"
                label="上传文件"
              >
                <Upload name="file" maxCount={1}>
                  <Button icon={<UploadOutlined/>}>点击选择文件</Button>
                </Upload>
              </Form.Item>
              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit">
                    提交
                  </Button>
                  <Button htmlType="reset">重置输入</Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="分析结论">
            <Paragraph copyable> {chart?.genResult ?? <div>请先在左侧进行提交</div>}</Paragraph>
            <Spin spinning={submitting}/>
          </Card>
          <Divider/>
          <Card title="可视化图表">
            {
              option ? <ReactEcharts option={option}/> : <div>请先在左侧进行提交</div>
            }
            <Spin spinning={submitting}/>
          </Card>
        </Col>

      </Row>

    </div>
  );
};
export default AddChart;
