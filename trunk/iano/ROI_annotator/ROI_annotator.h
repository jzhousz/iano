/* Jie Zhou March 2010
 *
 */

#ifndef __ROIANNOTATOR_H__
#define __ROIANNOTATOR_H__

//
//  Annotate/label regions of interest on a new 3D image (images) based on some existing labelled 3D images.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include <v3d_interface.h>

class ROIAnnotatorPlugin : public QObject, public V3DPluginInterface
{
    Q_OBJECT
    Q_INTERFACES(V3DPluginInterface)

public:
	QStringList menulist() const;
	void domenu(const QString &menu_name, V3DPluginCallback &callback, QWidget *parent);

	QStringList funclist() const {return QStringList();}
	void dofunc(const QString &func_name, const V3DPluginArgList &input, V3DPluginArgList &output, QWidget *parent) {}
};

#endif



