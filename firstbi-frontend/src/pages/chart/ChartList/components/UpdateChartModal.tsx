import CodeEditor from '@/components/CodeEditor';

import {Button, Card, Col, message, Modal, Row} from 'antd';
import ReactECharts from 'echarts-for-react';
import React, {useState} from 'react';
import {updateChartUsingPost} from "@/services/firstbi/chartController";

interface UpdateModalProps {
  oldData: API.Chart;
  modalVisible: boolean;
  // columns: ProColumns<API.Chart>[];
  onSubmit: () => void;
  onCancel: () => void;
}

/**
 * 更新数据模态框
 * @param fields
 */
const handleUpdate = async (fields: API.Chart) => {
  const hide = message.loading('正在更新');
  try {
    await updateChartUsingPost(fields);
    hide();
    message.success('更新成功');
    return true;
  } catch (error) {
    hide();
    message.error('更新失败请重试！');
    return false;
  }
};

/**
 * 更新数据模态框
 * @param props
 * @constructor
 */
const UpdateChartModal: React.FC<UpdateModalProps> = (props) => {
  const {oldData, modalVisible, onSubmit, onCancel} = props;
  const [newChartValue, setNewChartValue] = useState<string>('');
  //图表数据
  let data = oldData.genChart ?? '';

  return (
    <Modal
      destroyOnClose
      centered
      width={'90%'}
      title="查看原始数据"
      open={modalVisible}
      onCancel={() => onCancel()}
      footer={null}
    >
      <Row gutter={24}>
        <Col span={12}>
          <Card
            title="原始数据"
            extra={
              <Button
                type={'primary'}
                onClick={() => {
                  const params = {
                    id: oldData.id,
                    genChart: oldData.genChart,
                  };
                  console.log('updateParams', params);
                  handleUpdate(params);
                }}
              >
                提交
              </Button>
            }
          >
            {data !== '' && (
              <CodeEditor
                value={data}
                language={'json'}
                onChange={(newValue: string) => {
                  console.log('newValue', newValue);
                  //注意 赋值必须赋给 genChart 赋给 data 图表数据不会动态更新
                  oldData.genChart = newValue;
                  setNewChartValue(newValue);
                  return;
                }}
              />
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card title="图表">
            {data !== '' && (
              <div>
                <ReactECharts
                  style={{
                    width: '600px',
                  }}
                // todo: 直接修改时如果json格式错误会直接报错
                  option={JSON.parse(data)}
                />
              </div>
            )}
          </Card>
        </Col>
      </Row>
    </Modal>
  );
};

export default UpdateChartModal;
